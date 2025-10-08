package com.example.gui;

public class ClientGUI extends AbstractWhiteBoardGUI {

    public ClientGUI() {
        super("Client Whiteboard");
    }

    @Override
    protected void broadcastClear() {
        // TODO send “clear” to server so it updates other clients
    }
}
