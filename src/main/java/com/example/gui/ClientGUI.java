package com.example.gui;

/**
 * Client GUI
 * 
 * @author Sally Arnold
 */
public class ClientGUI extends AbstractWhiteBoardGUI {
    public ClientGUI() {
        super("Client Whiteboard");
        userList.setCellRenderer(new UserListCellRenderer(false));
    }
}
