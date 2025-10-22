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
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class WhiteBoardServer extends UnicastRemoteObject implements WhiteBoardServerInterface {
    ServerGUI gui;
    private final Map<String, WhiteBoardClientInterface> connectedClients = new ConcurrentHashMap<>();
    private final Map<String, WhiteBoardClientInterface> pendingClients = new ConcurrentHashMap<>();
    private List<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
    private int port;
    private String hostUsername;
    private ServerEventListener eventListener;

    protected WhiteBoardServer(int port, String hostUsername) throws RemoteException {
        super();
        this.port = port;
        this.hostUsername = hostUsername;
        gui = new ServerGUI(this); 
    }

    public void setEventListener(ServerEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public synchronized void registerClient(String username, WhiteBoardClientInterface client)
            throws RemoteException {

        if (username == null) {
            client.onRegistrationRejected("Please provide username.");
            return;
        }

        if (client == null) {
            return;
        }

        if (username.equals(hostUsername) || connectedClients.containsKey(username)
                || pendingClients.containsKey(username)) {
            client.onRegistrationRejected("Username is already taken. Please choose a different username.");
            return;
        }

        // Temporarily store as pending
        pendingClients.put(username, client);

        // Notify the GUI asynchronously
        if (eventListener != null) {
            eventListener.onJoinRequest(username);
        }

        // The GUI will call approveClient(username) or denyClient(username)
    }

    public synchronized void approveClient(String username) {
        WhiteBoardClientInterface client = pendingClients.remove(username);
        if (client != null) {
            try {
                client.onRegistrationApproved();

            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + username);
                // Don't add to list of connected clients
                return;
            }
            connectedClients.put(username, client);
            broadcastUserList();
            System.out.println("Approved client: " + username);

        }
    }

    public synchronized void denyClient(String username, String reason) {
        try {
            pendingClients.get(username).onRegistrationRejected(reason);
        } catch (RemoteException e) {
            // Do nothing if unable to reject a client
            System.err.println("[WARN] Lost connection to client: " + username);
        }
        pendingClients.remove(username);
        System.out.println("Denied client: " + username);
    }

    @Override
    public synchronized void unregisterClient(String username) throws RemoteException {
        connectedClients.remove(username);
        broadcastUserList();
        System.out.println("Client disconnected: " + username);
    }

    @Override
    public synchronized void kickUser(String username) throws RemoteException {
        WhiteBoardClientInterface client = connectedClients.remove(username);
        if (client != null) {
            try {
                client.kickedFromServer();
            } catch (RemoteException e) {
                // Do nothing
                System.err.println("Error notifying kicked client: " + e.getMessage());
            }
            broadcastUserList();
        }
    }

    @Override
    public synchronized void sendChatMessage(ChatMessage message) throws RemoteException {
        System.out.println("Message received: " + message);
        // Store the message in chat history
        chatHistory.add(message);

        // Add to server GUI chat panel
        gui.getChatPanel().appendMessage(message.toString());

        // Send to all client chat windows
        System.out.println("Updating client chat windows...");
        ArrayList<String> disconnectedClients = new ArrayList<>();
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.receiveChatMessage(message);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + user);
                // Mark for removal later
                disconnectedClients.add(user);
            }
        }

        if (disconnectedClients.size() > 0) {
            disconnectedClients.forEach(u -> connectedClients.remove(u));
            broadcastUserList();
        }

    }

    @Override
    public synchronized void broadcastDrawAction(DrawAction action) throws RemoteException {
        // Update the server GUI canvas
        System.out.println("Updating server canvas...");
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));

        // Update client canvases
        System.out.println("Updating client canvases...");
        ArrayList<String> disconnectedClients = new ArrayList<>();
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.receiveDrawAction(action);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + user);
                // Mark for removal later
                disconnectedClients.add(user);
            }
        }
        if (disconnectedClients.size() > 0) {
            disconnectedClients.forEach(u -> connectedClients.remove(u));
            broadcastUserList();
        }
    }

    @Override
    public synchronized void broadcastCursorPosition(CursorPosition pos) throws RemoteException {
        // Update server cursor positions list
        SwingUtilities.invokeLater(() -> gui.getCanvas().updateCursorPosition(pos));

        // Update clients cursor positions list
        ArrayList<String> disconnectedClients = new ArrayList<>();
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.updateCursorPosition(pos);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + user);
                // Mark for removal later
                disconnectedClients.add(user);
            }
        }

        if (disconnectedClients.size() > 0) {
            disconnectedClients.forEach(u -> connectedClients.remove(u));
            broadcastUserList();
        }
    }

    private synchronized void broadcastUserList() {
        ArrayList<String> fullUsersList = new ArrayList<>(connectedClients.keySet());
        // Add server owner's username to client usernames
        fullUsersList.addFirst(hostUsername);
        UserListUpdate update = new UserListUpdate(fullUsersList);
        // Update server gui
        SwingUtilities.invokeLater(() -> gui.updateUserList(update.getUsernames()));
        // Update clients gui
        ArrayList<String> disconnectedClients = new ArrayList<>();
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.receiveUserListUpdate(update);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + user);
                // Mark for removal later
                disconnectedClients.add(user);
            }
        }
        if (disconnectedClients.size() > 0) {
            disconnectedClients.forEach(u -> connectedClients.remove(u));
            broadcastUserList();
        }
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
        // Send out updated canvas
        // Update clients gui
        byte[] imageData = getCanvasSnapshot();
        ArrayList<String> disconnectedClients = new ArrayList<>();
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.receiveCanvasImage(imageData);
            } catch (RemoteException e) {
                System.err.println("[WARN] Lost connection to client: " + user);
                // Mark for removal later
                disconnectedClients.add(user);
            }
        }

        if (disconnectedClients.size() > 0) {
            disconnectedClients.forEach(u -> connectedClients.remove(u));
            broadcastUserList();
        }
    }

    public synchronized void notifyShutdown(String reason) {
        System.out.println("[INFO] Notifying clients of server shutdown...");
        for (var entry : connectedClients.entrySet()) {
            String user = entry.getKey();
            WhiteBoardClientInterface client = entry.getValue();
            try {
                client.onServerShutdown(reason);
            } catch (RemoteException e) {
                // No need to remove client here — server is shutting down anyway
                System.err.println("[WARN] Could not notify " + user + " of shutdown.");
            }
        }
    }

    public synchronized void shutdownServer() {
        notifyShutdown("Host has closed the whiteboard.");
        try {
            UnicastRemoteObject.unexportObject(this, true);
            LocateRegistry.getRegistry(port).unbind("WhiteBoardService");
            System.out.println("Server shut down cleanly.");
        } catch (Exception e) {
            System.err.println("Error shutting down server: " + e.getMessage());
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
                    // Remote exception handled in method itself
                    System.err.println("An error occured while broadcasting draw action.");
                }
            });

            // Send server cursor position to clients
            gui.getCanvas().setCursorListener(pos -> {
                try {
                    broadcastCursorPosition(pos);
                } catch (Exception e) {
                    // Remote exception handled in method itself
                    System.err.println("An error occured while broadcasting cursor position.");
                }
            });

            // Send messages to clients
            gui.getChatPanel().setChatListener(message -> {
                // Send message to server via RMI
                System.out.println("Sending: " + message);
                try {
                    sendChatMessage(new ChatMessage(hostUsername, message));
                } catch (RemoteException e) {
                    // Remote exception handled in method itself
                    System.err.println("An error occured while sending chat message.");
                }
            });

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
