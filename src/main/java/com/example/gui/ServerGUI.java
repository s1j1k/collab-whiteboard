package com.example.gui;


public class ServerGUI extends AbstractWhiteBoardGUI {

     // GUI Components
     WhiteBoardCanvas canvas = new WhiteBoardCanvas(500, 500);

     public ServerGUI() {
        super("Server Whiteboard");
    }

    //  @Override
    // protected void broadcastClear() {
    //     // TODO send a “clear” message to all clients
    // }

}

