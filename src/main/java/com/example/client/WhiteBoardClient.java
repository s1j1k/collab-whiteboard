package com.example.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.example.gui.ClientGUI;
import com.example.gui.WaitingScreen;
import com.example.shared.ChatMessage;
import com.example.shared.CursorPosition;
import com.example.shared.DrawAction;
import com.example.shared.UserListUpdate;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteBoardServerInterface;

public class WhiteBoardClient extends UnicastRemoteObject implements WhiteBoardClientInterface {

    private String host;
    private int port;
    private String username;
    private ClientGUI gui;
    WaitingScreen waitingScreen;
    private WhiteBoardServerInterface serverRef;
    private WhiteBoardClientInterface clientStub;

    public WhiteBoardClient(String host, int port) throws RemoteException {
        super();
        this.host = host;
        this.port = port;
        this.clientStub = this;

        gui = new ClientGUI();

        waitingScreen = new WaitingScreen(onConnect());
    }

    @Override
    public void onRegistrationApproved() throws RemoteException {
        System.out.println("Approved by server!");
        SwingUtilities.invokeLater(() -> {
            // close waiting screen and launch GUI
            if (waitingScreen != null) {
                waitingScreen.dispose();
            }
            launchClientGui();
        });
    }

    @Override
    public void onRegistrationRejected(String reason) throws RemoteException {
        System.out.println("Rejected by server: " + reason);
        SwingUtilities.invokeLater(() -> {
            if (waitingScreen != null) {
                waitingScreen.showRejection("Rejected: " + reason);
            } else {
                JOptionPane.showMessageDialog(null, "Rejected: " + reason);
            }
        });
    }

    @Override
    public void receiveDrawAction(DrawAction action) throws RemoteException {
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));
    }

    @Override
    public void receiveChatMessage(ChatMessage message) throws RemoteException {
        gui.getChatPanel().appendMessage(message.toString());
    }

    @Override
    public void updateCursorPosition(CursorPosition pos) throws RemoteException {
        SwingUtilities.invokeLater(() -> gui.getCanvas().updateCursorPosition(pos));
    }

    @Override
    public void receiveUserListUpdate(UserListUpdate update) throws RemoteException {
        SwingUtilities.invokeLater(() -> gui.updateUserList(update.getUsernames()));
    }

    @Override
    public void kickedFromServer() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "You’ve been removed from the whiteboard by the host.",
                    "Removed", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        });
    }

    // TODO WRITE lock - server side / client side (?)
    @Override
    public void receiveCanvasImage(byte[] imageData) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            gui.getCanvas().loadCanvasImage(imageData);
        });
    }

    /** Connect to server RMI service */
    public void connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        serverRef = (WhiteBoardServerInterface) registry.lookup("WhiteBoardService");
    }

    /** Send registration request to server for approval */
    public void register(String username, WhiteBoardClientInterface clientStub) throws RemoteException {
        this.username = username;
        serverRef.registerClient(username, clientStub);
    }

    public WhiteBoardServerInterface getServerRef() {
        return serverRef;
    }

    public WhiteBoardClientInterface getClientStub() {
        return clientStub;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Defines what happens when the user clicks "Connect"
     */
    private ActionListener onConnect() {
        return event -> {
            JButton sourceButton = (JButton) event.getSource();
            WaitingScreen waitingScreen = (WaitingScreen) SwingUtilities.getWindowAncestor(sourceButton);

            String username = waitingScreen.getUsername();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(waitingScreen, "Please enter a username.");
                return;
            }

            waitingScreen.showWaitingState();

            // Send registration request to server and wait for approval
            try {
                register(username, getClientStub());
            } catch (RemoteException e) {
                handleServerDisconnect(e);
            }

        };
    }

    /** Exit the application when server shuts down */
    private void handleServerDisconnect(Exception e) {
        System.err.println("[ERROR] Lost connection to server: " + e.getMessage());
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gui,
                    "Connection to the whiteboard server was lost.\n" +
                            "The application will now close.",
                    "Disconnected", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }

    public void launchWaitingScreen() {
        // Launch Waiting Screen safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            waitingScreen.setVisible(true);
        });

    }

    public void launchClientGui() {
        // Fetch initial state
        ArrayList<ChatMessage> history;
        byte[] snapshot;
        try {
            history = serverRef.getChatHistory();
            snapshot = serverRef.getCanvasSnapshot();
            // Update GUI safely
            SwingUtilities.invokeLater(() -> {
                gui.getChatPanel().loadHistory(history);
                gui.getCanvas().loadCanvasImage(snapshot);
            });
        } catch (RemoteException e) {
            handleServerDisconnect(e);
        }

        // Launch Server GUI safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
        });

        // Set username in the canvas
        gui.getCanvas().setUsername(username);

        // Send draw actions to server
        gui.getCanvas().setDrawListener(action -> {
            try {
                getServerRef().broadcastDrawAction(action);
            } catch (Exception e) {
                handleServerDisconnect(e);
            }
        });

        // Send cursor position to server
        gui.getCanvas().setCursorListener(pos -> {
            try {
                getServerRef().broadcastCursorPosition(pos);
            } catch (Exception e) {
                handleServerDisconnect(e);
            }
        });

        // Send messages to server via RMI
        gui.getChatPanel().setChatListener(message -> {
            System.out.println("Sending: " + message);
            try {
                getServerRef().sendChatMessage(new ChatMessage(username, message));
            } catch (RemoteException e) {
                handleServerDisconnect(e);
            }
        });

    }
}
