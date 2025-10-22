package com.example.client;

import javax.swing.JOptionPane;

/**
 * 
 * Client entry point
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class JoinWhiteBoard {

    public static void main(String[] args) {
        // Parse command line arguments
        // Expected: java JoinWhiteBoard <serverIPAddress> <serverPort>
        String serverAddress;
        int port;
        try {
            serverAddress = new String(args[0]);
            port = Integer.parseInt(args[1]);
        } catch (Throwable e) {
            System.out
                    .println("Pleas provide command line arguments: <server IP address> <port number>.");
            throw new RuntimeException(e);
        }

        try {
            WhiteBoardClient client = new WhiteBoardClient(serverAddress, port);
            // Connect to network
            client.connect();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Client closing...");
                try {
                    client.getServerRef().unregisterClient(client.getUsername());
                } catch (Exception ignored) {
                }
            }));

            // Launch gui entry point
            client.launchWaitingScreen();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage());

        }

    }

}
