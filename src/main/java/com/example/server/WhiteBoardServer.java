package com.example.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.example.gui.ServerGUI;
import com.example.shared.ChatMessage;
import com.example.shared.CursorPosition;
import com.example.shared.DrawAction;
import com.example.shared.ServerEventListener;
import com.example.shared.UserListUpdate;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteBoardServerInterface;

/**
 * Whiteboard RMI Server
 *
 * Concurrency model: the server lock only guards shared state (client map,
 * chat history) and the order in which updates are queued. Remote calls to
 * clients never run under the lock. Each client has its own single-threaded
 * outbound queue, so updates reach every client in the same order, and a
 * slow or dead client cannot stall the other clients or the host's GUI.
 *
 * @author Sally Arnold
 */
public class WhiteBoardServer extends UnicastRemoteObject implements WhiteBoardServerInterface {
    ServerGUI gui;
    private final Map<String, ClientChannel> connectedClients = new ConcurrentHashMap<>();
    private final Map<String, WhiteBoardClientInterface> pendingClients = new ConcurrentHashMap<>();
    private final List<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
    private int port;
    private String hostUsername;
    private ServerEventListener eventListener;

    /** A remote call to make on one client. */
    @FunctionalInterface
    interface ClientCall {
        void invoke(WhiteBoardClientInterface client) throws RemoteException;
    }

    /** Outbound queue for one client: calls run in order on a dedicated thread. */
    private final class ClientChannel {
        final String username;
        final WhiteBoardClientInterface stub;
        final ExecutorService outbox;

        ClientChannel(String username, WhiteBoardClientInterface stub) {
            this.username = username;
            this.stub = stub;
            this.outbox = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "outbox-" + username);
                t.setDaemon(true);
                return t;
            });
        }

        void send(ClientCall call) {
            if (outbox.isShutdown()) {
                return;
            }
            outbox.execute(() -> {
                try {
                    call.invoke(stub);
                } catch (RemoteException e) {
                    System.err.println("[WARN] Lost connection to client: " + username);
                    dropClient(username, this);
                }
            });
        }
    }

    protected WhiteBoardServer(int port, String hostUsername) throws RemoteException {
        super();
        this.port = port;
        this.hostUsername = hostUsername;
        gui = new ServerGUI(this);
    }

    public void setEventListener(ServerEventListener listener) {
        this.eventListener = listener;
    }

    /** Queue a call for every connected client. Never blocks on the network. */
    private void broadcast(ClientCall call) {
        for (ClientChannel channel : connectedClients.values()) {
            channel.send(call);
        }
    }

    /** Remove a client whose channel failed, unless it was already replaced or removed. */
    private void dropClient(String username, ClientChannel channel) {
        synchronized (this) {
            if (!connectedClients.remove(username, channel)) {
                return;
            }
            broadcastUserList();
        }
        channel.outbox.shutdown();
    }

    @Override
    public void registerClient(String username, WhiteBoardClientInterface client)
            throws RemoteException {
        if (client == null) {
            return;
        }

        String rejection = null;
        synchronized (this) {
            if (username == null) {
                rejection = "Please provide username.";
            } else if (username.equals(hostUsername) || connectedClients.containsKey(username)
                    || pendingClients.containsKey(username)) {
                rejection = "Username is already taken. Please choose a different username.";
            } else {
                // Hold as pending until the host approves or denies
                pendingClients.put(username, client);
            }
        }

        if (rejection != null) {
            client.onRegistrationRejected(rejection);
            return;
        }

        // The GUI will call approveClient(username) or denyClient(username)
        if (eventListener != null) {
            eventListener.onJoinRequest(username);
        }
    }

    public synchronized void approveClient(String username) {
        WhiteBoardClientInterface client = pendingClients.remove(username);
        if (client != null) {
            ClientChannel channel = new ClientChannel(username, client);
            // Approval is queued first, so it reaches the client before any broadcast
            channel.send(WhiteBoardClientInterface::onRegistrationApproved);
            connectedClients.put(username, channel);
            broadcastUserList();
            System.out.println("Approved client: " + username);
        }
    }

    public void denyClient(String username, String reason) {
        WhiteBoardClientInterface client = pendingClients.remove(username);
        if (client == null) {
            return;
        }
        // Notify off the GUI thread so a dead client cannot freeze the host
        new Thread(() -> {
            try {
                client.onRegistrationRejected(reason);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + username);
            }
        }, "deny-" + username).start();
        System.out.println("Denied client: " + username);
    }

    @Override
    public void unregisterClient(String username) throws RemoteException {
        ClientChannel channel;
        synchronized (this) {
            channel = connectedClients.remove(username);
            if (channel == null) {
                return;
            }
            broadcastUserList();
        }
        channel.outbox.shutdown();
        System.out.println("Client disconnected: " + username);
    }

    @Override
    public void kickUser(String username) throws RemoteException {
        ClientChannel channel;
        synchronized (this) {
            channel = connectedClients.remove(username);
            if (channel == null) {
                return;
            }
            broadcastUserList();
        }
        channel.send(WhiteBoardClientInterface::kickedFromServer);
        channel.outbox.shutdown();
    }

    @Override
    public synchronized void sendChatMessage(ChatMessage message) throws RemoteException {
        chatHistory.add(message);
        SwingUtilities.invokeLater(() -> gui.getChatPanel().appendMessage(message.toString()));
        broadcast(c -> c.receiveChatMessage(message));
    }

    @Override
    public synchronized void broadcastDrawAction(DrawAction action) throws RemoteException {
        // Apply on the server canvas and queue for clients under one lock, so the
        // server and every client see draw actions in the same order
        SwingUtilities.invokeLater(() -> {
            gui.getCanvas().applyDrawAction(action);
            gui.markModified();
        });
        broadcast(c -> c.receiveDrawAction(action));
    }

    @Override
    public void broadcastCursorPosition(CursorPosition pos) throws RemoteException {
        // Cursor positions are cosmetic and frequent: no ordering lock needed
        SwingUtilities.invokeLater(() -> gui.getCanvas().updateCursorPosition(pos));
        broadcast(c -> c.updateCursorPosition(pos));
    }

    private synchronized void broadcastUserList() {
        ArrayList<String> fullUsersList = new ArrayList<>(connectedClients.keySet());
        // Add server owner's username to client usernames
        fullUsersList.addFirst(hostUsername);
        UserListUpdate update = new UserListUpdate(fullUsersList);
        SwingUtilities.invokeLater(() -> gui.updateUserList(update.getUsernames()));
        broadcast(c -> c.receiveUserListUpdate(update));
    }

    @Override
    public synchronized ArrayList<ChatMessage> getChatHistory() throws RemoteException {
        return new ArrayList<>(chatHistory);
    }

    @Override
    public synchronized byte[] getCanvasSnapshot() throws RemoteException {
        BufferedImage canvasImage = gui.getCanvas().getCanvasImage();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(canvasImage, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RemoteException("Failed to encode canvas", e);
        }
    }

    public synchronized void broadcastCanvasImage() throws RemoteException {
        byte[] imageData = getCanvasSnapshot();
        broadcast(c -> c.receiveCanvasImage(imageData));
    }

    /** Tell every client the host is leaving, waiting briefly for delivery. */
    public void notifyShutdown(String reason) {
        System.out.println("Notifying clients of server shutdown...");
        List<ClientChannel> channels;
        synchronized (this) {
            channels = new ArrayList<>(connectedClients.values());
            connectedClients.clear();
        }
        for (ClientChannel channel : channels) {
            channel.send(c -> c.onServerShutdown(reason));
            channel.outbox.shutdown();
        }
        for (ClientChannel channel : channels) {
            try {
                if (!channel.outbox.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("[WARN] Could not notify " + channel.username + " of shutdown.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void shutdownServer() {
        notifyShutdown("Host has closed the whiteboard.");
        try {
            UnicastRemoteObject.unexportObject(this, true);
            LocateRegistry.getRegistry(port).unbind("WhiteBoardService");
            System.out.println("Server shut down cleanly.");
        } catch (Exception e) {
            System.err.println("[ERROR] Error shutting down server: " + e.getMessage());
        }
    }

    public String getUsername() {
        return hostUsername;
    }

    public void start() {
        try {
            // Create and bind the remote object in the RMI registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("WhiteBoardService", this);
            System.out.println("Whiteboard Server is ready.");

            // Launch Server GUI safely on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> gui.setVisible(true));

            // Display username in users list (Even if nobody joined yet)
            broadcastUserList();

            // Set username in GUI
            gui.getCanvas().setUsername(hostUsername);

            // Whenever server draws locally, send it to all of the clients
            gui.getCanvas().setDrawListener(action -> {
                try {
                    broadcastDrawAction(action);
                } catch (Exception e) {
                    System.err.println("[ERROR] An error occured while broadcasting draw action.");
                }
            });

            // Send server cursor position to clients
            gui.getCanvas().setCursorListener(pos -> {
                try {
                    broadcastCursorPosition(pos);
                } catch (Exception e) {
                    System.err.println("[ERROR] An error occured while broadcasting cursor position.");
                }
            });

            // Send messages to clients
            gui.getChatPanel().setChatListener(message -> {
                System.out.println("Sending: " + message);
                try {
                    sendChatMessage(new ChatMessage(hostUsername, message));
                } catch (RemoteException e) {
                    System.err.println("[ERROR] An error occured while sending chat message.");
                }
            });

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
