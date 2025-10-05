package com.example;

public class CreateWhiteBoard {
    
}


// TODO parse command line arguments
    /**
     * Main method for testing GUI
     * You should modify this to include command line argument parsing
     */
    public static void main(String[] args) {
        // Parse command line arguments
        // Expected:  java CreateWhiteBoard <serverIPAddress> <serverPort> username
        String serverAddress;
        int port;
        String username;

        // FIXME detailed error handling
        try {
            serverAddress = new String(args[0]);
            port = Integer.parseInt(args[1]);
            username = new String(args[2]);
        } catch (Throwable e) {
            logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

    }
