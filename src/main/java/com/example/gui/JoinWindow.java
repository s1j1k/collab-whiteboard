package com.example.gui;

import com.example.client.WhiteBoardClient;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class JoinWindow extends JFrame {
    private final WhiteBoardClient client;
    private JTextField usernameField;
    private JButton joinButton;

    public JoinWindow(WhiteBoardClient client) {
        this.client = client;
        initUI();
    }

    private void initUI() {
        setTitle("Join Whiteboard");
        setSize(300, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel label = new JLabel("Enter username:");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        usernameField = new JTextField();
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        add(usernameField, BorderLayout.CENTER);

        joinButton = new JButton("Join");
        add(joinButton, BorderLayout.SOUTH);

        joinButton.addActionListener(e -> attemptJoin());
    }

    private void attemptJoin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        joinButton.setEnabled(false);

        new Thread(() -> {
            try {
                // Register the client
                boolean success = client.register(username, client.getClientStub());
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Joined successfully!");
                        openClientGUI();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Username already taken.");
                        joinButton.setEnabled(true);
                    });
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
                    joinButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void openClientGUI() {
        // Close join window
        dispose();

        // Launch whiteboard gui
        client.launchClientGui();
    }
}
