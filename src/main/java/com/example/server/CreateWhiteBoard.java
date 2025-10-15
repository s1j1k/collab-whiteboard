package com.example.server;

// FIXME logger not installed correctly

// import org.apache.logging.log4j.Logger;
// import org.apache.logging.log4j.LogManager;

public class CreateWhiteBoard {

    // Logger
    // private static Logger logger = LogManager.getLogger(CreateWhiteBoard.class);

    // TODO parse command line arguments
    /**
     * Main method for testing GUI
     * You should modify this to include command line argument parsing
     */
    public static void main(String[] args) {
        // Parse command line arguments
        // Expected: java CreateWhiteBoard <serverIPAddress> <serverPort> username
        String host;
        int port;
        String username;

        // FIXME detailed error handling
        try {
            host = new String(args[0]);
            port = Integer.parseInt(args[1]);
            username = new String(args[2]);
        } catch (Throwable e) {
            // logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

        try {
            // Start the RMI whiteboard server
            // FIXME do this on a separate thread
            WhiteBoardServer server = new WhiteBoardServer(host, port, username);
            // FIXME need host name?
            // TODO add drawing interface etc. etc.
            server.start();

            

            // FIXME move this to server start function itself?
            // Launch Server GUI safely on the Event Dispatch Thread
            // SwingUtilities.invokeLater(() -> new ServerGUI().setVisible(true));

            System.out.println("Whiteboard server started successfully.");
        } catch (Exception e) {
            System.err.println("Failed to start Whiteboard Server: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
