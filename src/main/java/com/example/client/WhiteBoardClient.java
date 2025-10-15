package com.example.client;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import com.example.gui.ClientGUI;
import com.example.shared.DrawAction;
import com.example.shared.WhiteBoardClientInterface;
import com.example.shared.WhiteboardServerInterface;

public class WhiteBoardClient extends UnicastRemoteObject implements WhiteBoardClientInterface {

    private String host;
    private int port;
    private String username;
    // FIXME is it ok to launch GUI here?
    private ClientGUI gui = new ClientGUI();

    public WhiteBoardClient(String host, int port, String username) throws RemoteException {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    // FIXME not implemented
    @Override
    public void receiveDrawAction(DrawAction action) throws RemoteException {
        SwingUtilities.invokeLater(() -> gui.getCanvas().applyDrawAction(action));
    }

    public void start() {

        try {
            // Launch the GUI
            // FIXME do this last 
            SwingUtilities.invokeLater(() ->  gui.setVisible(true));

            // Get the RMI registry and the remote object
            Registry registry = LocateRegistry.getRegistry(host, port);
            WhiteboardServerInterface server = (WhiteboardServerInterface) registry.lookup("MessageService");

            // Register client
            server.registerClient(this);

            // TODO init canvas with what's on server already

             // Whenever client draws locally, send it to the server
            gui.getCanvas().setDrawListener(action -> {
                try {
                    server.broadcastDrawAction(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // show the client GUI containing this canvas
            System.out.println("✅ Connected to server");


            // Scanner scanner = new Scanner(System.in);
            // System.out.print("Enter your username: ");
            // String username = scanner.nextLine();

            // FIXME implement this in the client GUI chat window instead
            // System.out.println("You can now send messages to the server. Type 'exit' to quit.");
            gui.getChatPanel().setChatListener(message -> {
                // send message to server via RMI 
                System.out.println("Sending: " + message);
                try {
                    server.sendMessage(username, message);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            
            // while (true) {
            //     System.out.print("Enter message: ");
            //     String message = scanner.nextLine();
            //     if ("exit".equalsIgnoreCase(message)) {
            //         break;
            //     }
            //     server.sendMessage(username, message);
            // }
            // scanner.close();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }
 
}
