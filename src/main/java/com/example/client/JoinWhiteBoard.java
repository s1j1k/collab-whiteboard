package com.example.client;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.example.gui.JoinWindow;

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
                    .println("Pleas provide command line arguments: server IP address and port number and try again.");
            throw new RuntimeException(e);
        }

        try {
            WhiteBoardClient client = new WhiteBoardClient(serverAddress, port);
            // Connect to server
            client.connect();

            SwingUtilities.invokeLater(() -> {
                JoinWindow joinWindow = new JoinWindow(client);
                joinWindow.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage());
        }

    }

}
