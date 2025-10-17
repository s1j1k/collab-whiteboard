package com.example.server;

// MessageServer.java
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import com.example.gui.ServerGUI;
import com.example.shared.ChatMessage;
import com.example.shared.DrawAction;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteBoardServerInterface;

public class WhiteBoardServer extends UnicastRemoteObject implements WhiteBoardServerInterface {
    ServerGUI gui = new ServerGUI();
    // private final List<WhiteBoardClientInterface> clients = new ArrayList<>();
    private final Map<String, WhiteBoardClientInterface> connectedClients = new ConcurrentHashMap<>();

    // FIXME so I can use the host
    private String host;
    private int port;
    private String username;

    protected WhiteBoardServer(String host, int port, String username) throws RemoteException {
        super();
        this.host = host;
        this.port = port;
        this.username = username;
    }

    @Override
    public synchronized boolean registerClient(String username, WhiteBoardClientInterface client)
            throws RemoteException {
        if (username == null || client == null) {
            // TODO share what happened to the client error message
            System.out.println("Registration failed: null username or client");
            return false;
        }

        if (connectedClients.containsKey(username)) {
            System.out.println("Registration failed: username already exists");
            return false;
        }

        connectedClients.put(username, client);
        System.out.println("Client registered: " + username);
        return true;
    }

    // TODO save server owner's username also
    @Override
    public void sendChatMessage(ChatMessage message) throws RemoteException {
        System.out.println("Message received: " + message);
        // Additional server-side logic here, e.g., logging or storing the message
        // TODO update chat window in server and all the clients
        // FIXME add some formatting / color
        gui.getChatPanel().appendMessage(message.toString());
        // Send to all clients

        // Update client chat windows
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
        System.out.println("Client disconnected: " + username);
    }

    public void start() {
        try {
            // Create and bind the remote object in the RMI registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("WhiteBoardService", this);
            System.out.println("Whiteboard Server is ready.");

            // Launch Server GUI safely on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> gui.setVisible(true));

            // Whenever server draws locally, send it to all of the clients
            gui.getCanvas().setDrawListener(action -> {
                try {
                    broadcastDrawAction(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            gui.getCanvas().setUsername(username);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
