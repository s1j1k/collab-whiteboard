package com.example.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.example.gui.ClientGUI;
import com.example.shared.ChatMessage;
import com.example.shared.DrawAction;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteBoardServerInterface;

public class WhiteBoardClient extends UnicastRemoteObject implements WhiteBoardClientInterface {

    private String host;
    private int port;
    private String username;
    // FIXME is it ok to launch GUI here?
    private ClientGUI gui = new ClientGUI();
    private WhiteBoardServerInterface serverRef;
    private WhiteBoardClientInterface clientStub;

    public WhiteBoardClient(String host, int port) throws RemoteException {
        super();
        this.host = host;
        this.port = port;
    }

    @Override
    public void receiveDrawAction(DrawAction action) throws RemoteException {
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));
    }

    @Override
    public void receiveChatMessage(ChatMessage message) throws RemoteException {
        gui.getChatPanel().appendMessage(message.toString());
    }

    public void connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        serverRef = (WhiteBoardServerInterface) registry.lookup("WhiteBoardService");
    }

    public boolean register(String username, WhiteBoardClientInterface clientStub) throws RemoteException {
        this.username = username;
        this.clientStub = clientStub;
        return serverRef.registerClient(username, clientStub);
    }

    public WhiteBoardServerInterface getServerRef() {
        return serverRef;
    }

    // FIXME don't use this?
    public WhiteBoardClientInterface getClientStub() {
        // TODO ensure its always this
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void launchClientGui() {

        // Launch Server GUI safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            gui.pack(); // respect preferred sizes
            gui.setLocationRelativeTo(null); // center on screen
            gui.setVisible(true);
            // gui.setVisible(true);
        });

        gui.getCanvas().setUsername(username);

        // Send draw actions to server
        gui.getCanvas().setDrawListener(action -> {
            try {
                getServerRef().broadcastDrawAction(action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Send messages to server
        gui.getChatPanel().setChatListener(message -> {
            // send message to server via RMI
            System.out.println("Sending: " + message);
            try {
                getServerRef().sendChatMessage(new ChatMessage(username, message));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

    }
}
