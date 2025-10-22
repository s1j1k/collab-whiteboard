package com.example.gui;

/**
 * Client GUI
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class ClientGUI extends AbstractWhiteBoardGUI {
    public ClientGUI() {
        super("Client Whiteboard");
        userList.setCellRenderer(new UserListCellRenderer(false));
    }
}
