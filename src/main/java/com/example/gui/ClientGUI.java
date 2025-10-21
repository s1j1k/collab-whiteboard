package com.example.gui;

public class ClientGUI extends AbstractWhiteBoardGUI {
    public ClientGUI() {
        super("Client Whiteboard");
        userList.setCellRenderer(new UserListCellRenderer(false));
    }
}

