package com.example.server;

// MessageServer.java
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.example.shared.RemoteMessageService;

public class WhiteBoardServer extends UnicastRemoteObject implements RemoteMessageService {

    // private String host;
    // private int port;

    protected WhiteBoardServer() throws RemoteException {
        // this.host = host;
        // this.port = port;
        super();
    }

    @Override
    public void sendMessage(String username, String message) throws RemoteException {
        System.out.println("Message received from " + username + ": " + message);
        // Additional server-side logic here, e.g., logging or storing the message
    }

    public void start(int port) {
        try {
                // Create and bind the remote object in the RMI registry
                // RemoteMessageService server = new WhiteBoardServer();
                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("MessageService", this);
                System.out.println("Message Server is ready.");
            } catch (Exception e) {
                System.err.println("Server exception: " + e.toString());
                e.printStackTrace();
            }
    }
}
