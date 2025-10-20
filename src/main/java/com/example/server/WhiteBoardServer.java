package com.example.server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
// MessageServer.java
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
import com.example.shared.UserListUpdate;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteBoardServerInterface;

public class WhiteBoardServer extends UnicastRemoteObject implements WhiteBoardServerInterface {
    // FIXME take username as a param
    ServerGUI gui = new ServerGUI(this);
    private final Map<String, WhiteBoardClientInterface> connectedClients = new ConcurrentHashMap<>();
    private List<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
    // private List<DrawAction> drawHistory = new ArrayList<DrawAction>();
    private int port;
    private String username;

    protected WhiteBoardServer(int port, String username) throws RemoteException {
        super();
        this.port = port;
        this.username = username;
    }

    @Override
    public synchronized boolean registerClient(String username, WhiteBoardClientInterface client)
            throws RemoteException {
        if (username == null || client == null) {
            // TODO share what happened to the client error message instead of
            System.out.println("Registration failed: null username or client");
            return false;
        }

        if (connectedClients.containsKey(username) || username.equals(this.username)) {
            System.out.println("Registration failed: username already exists");
            return false;
        }

        connectedClients.put(username, client);
        broadcastUserList();
        System.out.println("Client registered: " + username);
        return true;
    }

    @Override
    public void sendChatMessage(ChatMessage message) throws RemoteException {
        System.out.println("Message received: " + message);
        // Store the message in chat history
        chatHistory.add(message);

        // TODO improvement: add some formatting / color
        gui.getChatPanel().appendMessage(message.toString());

        // Send to all client chat windows
        System.out.println("Updating client chat windows...");
        for (WhiteBoardClientInterface client : connectedClients.values()) {
            try {
                client.receiveChatMessage(message);
            } catch (RemoteException e) {
                System.err.println("Failed to send action to a client: " + e.getMessage());
            }
        }
    }

    // FIXME make it thread safe
    @Override
    public synchronized void broadcastDrawAction(DrawAction action) throws RemoteException {
        // Update the server GUI canvas
        System.out.println("Updating server canvas...");
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));

        // Update client canvases
        System.out.println("Updating client canvases...");
        for (WhiteBoardClientInterface client : connectedClients.values()) {
            try {
                client.receiveDrawAction(action);
            } catch (RemoteException e) {
                System.err.println("Failed to send action to a client: " + e.getMessage());
            }
        }
    }

    // TODO include in client finally block
    @Override
    public synchronized void unregisterClient(String username) throws RemoteException {
        connectedClients.remove(username);
        broadcastUserList();
        System.out.println("Client disconnected: " + username);
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

    @Override
    public synchronized void broadcastCursorPosition(CursorPosition pos) throws RemoteException {
        // Update server cursor positions list
        SwingUtilities.invokeLater(() -> gui.getCanvas().updateCursorPosition(pos));

        // Update clients cursor positions list
        for (WhiteBoardClientInterface c : connectedClients.values()) {
            try {
                c.updateCursorPosition(pos);
            } catch (RemoteException e) {
                System.err.println("Failed to update cursor for client: " + e.getMessage());
            }
        }
    }

    private void broadcastUserList() {
        UserListUpdate update = new UserListUpdate(new ArrayList<>(connectedClients.keySet()));
        // Update server gui
        SwingUtilities.invokeLater(() -> gui.updateUserList(update.getUsernames()));
        // Update clients gui
        for (WhiteBoardClientInterface c : connectedClients.values()) {
            try {
                c.receiveUserListUpdate(update);
            } catch (RemoteException e) {
                System.err.println("Failed to update user list: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void kickUser(String username) throws RemoteException {
        WhiteBoardClientInterface client = connectedClients.remove(username);
        if (client != null) {
            try {
                client.kickedFromServer(); // optional client callback
            } catch (RemoteException e) {
                System.err.println("Error notifying kicked client: " + e.getMessage());
            }
            broadcastUserList();
        }
    }

    public void start() {
        try {
            // Create and bind the remote object in the RMI registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("WhiteBoardService", this);
            System.out.println("Whiteboard Server is ready.");

            // Launch Server GUI safely on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> gui.setVisible(true));

            // FIXME add this to initialization/constructor of the GUI
            gui.getCanvas().setUsername(username);

            // Whenever server draws locally, send it to all of the clients
            gui.getCanvas().setDrawListener(action -> {
                try {
                    broadcastDrawAction(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Send server cursor position to clients
            gui.getCanvas().setCursorListener(pos -> {
                try {
                    broadcastCursorPosition(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Send messages to clients
            gui.getChatPanel().setChatListener(message -> {
                // send message to server via RMI
                System.out.println("Sending: " + message);
                try {
                    sendChatMessage(new ChatMessage(username, message));
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
