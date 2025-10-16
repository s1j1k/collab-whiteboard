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
    private ClientGUI gui;
    private WhiteBoardServerInterface serverRef;
    private WhiteBoardClientInterface clientStub;

    public WhiteBoardClient(String host, int port) throws RemoteException {
        super();
        this.host = host;
        this.port = port;
        // this.username = username;
        gui = new ClientGUI(this);
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

    public WhiteBoardClientInterface getClientStub() {
        return clientStub;
    }

    public String getUsername() {
        return username;
    }



    public void start() {

        try {
            // Launch the GUI
            // FIXME do this last ?
            SwingUtilities.invokeLater(() -> gui.setVisible(true));

            // TODO init canvas with what's on server already
            // FIXME add to when gui gets init
            // Whenever client draws locally, send it to the server
            gui.getCanvas().setDrawListener(action -> {
                try {
                    getServerRef().broadcastDrawAction(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

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

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }

}
