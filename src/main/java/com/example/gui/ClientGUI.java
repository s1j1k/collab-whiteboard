package com.example.gui;

import java.awt.BorderLayout;
import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.example.client.WhiteBoardClient;

public class ClientGUI extends AbstractWhiteBoardGUI {
    private final WhiteBoardClient whiteboardClient;
    private JoinOverlayPanel joinOverlay;

    public ClientGUI(WhiteBoardClient client) {
        super("Client Whiteboard");
        this.whiteboardClient = client;
        joinOverlay = new JoinOverlayPanel(this::attemptJoin);
        getContentPane().add(joinOverlay, BorderLayout.CENTER);
    }


    private void attemptJoin(String username) {
    joinOverlay.setEnabledState(false);
    new Thread(() -> {
        try {
            boolean success = whiteboardClient.register(username, whiteboardClient.getClientStub());
            if (success) {
                SwingUtilities.invokeLater(() -> {
                    joinOverlay.setVisible(false);
                    canvas.setUsername(username);
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Username already taken.");
                    joinOverlay.setEnabledState(true);
                });
            }
        } catch (RemoteException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Failed to connect to server.");
                joinOverlay.setEnabledState(true);
            });
        }
    }).start();
}

    // @Override
    // protected void broadcastClear() {
    //     // TODO send “clear” to server so it updates other clients
    // }
}
