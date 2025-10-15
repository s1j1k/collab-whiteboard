package com.example.server;

// MessageServer.java
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.example.gui.ServerGUI;
import com.example.shared.DrawAction;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteboardServerInterface;

public class WhiteBoardServer extends UnicastRemoteObject implements WhiteboardServerInterface {
    ServerGUI gui = new ServerGUI();
    private final List<WhiteBoardClientInterface> clients = new ArrayList<>();

    protected WhiteBoardServer() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerClient(WhiteBoardClientInterface client) throws RemoteException {
        clients.add(client);
        System.out.println("Client registered: " + client);
    }

    @Override
    public void sendMessage(String username, String message) throws RemoteException {
        System.out.println("Message received from " + username + ": " + message);
        // Additional server-side logic here, e.g., logging or storing the message
    }

    /** 
     * Client sends this drawing to the server
     */
    // @Override
    // public void receiveDrawAction(DrawAction action) throws RemoteException {
    //     SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));
    // }

    // FIXME make it thread safe
    @Override
    public synchronized void broadcastDrawAction(DrawAction action) throws RemoteException {
        // Update the server GUI canvas
        System.out.println("Updating server canvas...");
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));

        // Update client canvases
        System.out.println("Updating client canvases...");
        for (WhiteBoardClientInterface client : clients) {
            try {
                client.receiveDrawAction(action);
            } catch (RemoteException e) {
                System.err.println("Failed to send action to a client: " + e.getMessage());
            }
        }
    }

    public void start(int port) {
        try {
            // Create and bind the remote object in the RMI registry
            // RemoteMessageService server = new WhiteBoardServer();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MessageService", this);
            System.out.println("Message Server is ready.");

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
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
