package com.example.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server entrypoint class
 * Launches Whiteboard RMI Server
 */
public class CreateWhiteBoard {
    public static void main(String[] args) {
        // Parse command line arguments
        // Expected: java CreateWhiteBoard <serverPort> username
        int port;
        String username;
        try {
            port = Integer.parseInt(args[0]);
            username = new String(args[1]);
        } catch (Throwable e) {
            System.out.println("Please provide command line arguments: <server port> <username>.");
            throw new RuntimeException(e);
        }

        try {
            // Start the RMI whiteboard server
            WhiteBoardServer server = new WhiteBoardServer(port, username);
            

            // Allow graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Server shutting down...");
                // FIXME notify when server exits on purpose
                server.shutdownServer();
            }));

            server.start();
            System.out.println("Whiteboard server started successfully.");
        } catch (Exception e) {
            System.err.println("Failed to start Whiteboard Server: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
