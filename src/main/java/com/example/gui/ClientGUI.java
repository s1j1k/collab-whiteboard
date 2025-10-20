package com.example.gui;

public class ClientGUI extends AbstractWhiteBoardGUI {
    public ClientGUI() {
        super("Client Whiteboard");
        // TODO init canvas with what's on server already
        userList.setCellRenderer(new UserListCellRenderer(false));

           
    }
}

