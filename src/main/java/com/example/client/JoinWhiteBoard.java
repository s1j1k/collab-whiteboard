package com.example.client;

import java.rmi.RemoteException;

public class JoinWhiteBoard {

    public static void main(String[] args) { 
        // Parse command line arguments
        // Expected: java JoinWhiteBoard <serverIPAddress> <serverPort> username
        String serverAddress;
        int port;
        String username;

        // FIXME detailed error handling
        try {
            serverAddress = new String(args[0]);
            port = Integer.parseInt(args[1]);
            username = new String(args[2]);
        } catch (Throwable e) {
            // logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

        // TODO launch client GUI as well
        // FIXME allow change of username?

        WhiteBoardClient client;
        try {
            client = new WhiteBoardClient(serverAddress, port, username);
            client.start();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    } 
    
}
