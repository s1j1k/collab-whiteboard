package com.example.client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import com.example.shared.RemoteMessageService;

public class WhiteBoardClient {

    private String host;
    private int port;
    private String username;

    public WhiteBoardClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void start() {

        try {
            // Get the RMI registry and the remote object
            Registry registry = LocateRegistry.getRegistry(host, port);
            RemoteMessageService server = (RemoteMessageService) registry.lookup("MessageService");

            Scanner scanner = new Scanner(System.in);
            // System.out.print("Enter your username: ");
            // String username = scanner.nextLine();

            // FIXME implement this in the client GUI chat window instead
            System.out.println("You can now send messages to the server. Type 'exit' to quit.");
            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                server.sendMessage(username, message);
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }
 
}
