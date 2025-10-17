package com.example.client;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.example.gui.ClientGUI;
import com.example.gui.JoinWindow;

public class JoinWhiteBoard {

    public static void main(String[] args) {
        // Parse command line arguments
        // Expected: java JoinWhiteBoard <serverIPAddress> <serverPort> username
        String serverAddress;
        int port;
        // String username;

        // FIXME detailed error handling
        try {
            serverAddress = new String(args[0]);
            port = Integer.parseInt(args[1]);
            // username = new String(args[2]);
        } catch (Throwable e) {
            // logger.error("An error occurred while parsing command line arguments.", e);
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
