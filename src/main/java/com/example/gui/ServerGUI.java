package com.example.gui;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.example.server.WhiteBoardServer;
import com.example.shared.ServerEventListener;

public class ServerGUI extends AbstractWhiteBoardGUI implements ServerEventListener {

    // Default file name
    private File whiteboardFile = new File("Whiteboard.png");

    WhiteBoardServer server;

    public ServerGUI(WhiteBoardServer server) {
        super("Server Whiteboard");

        this.server = server;
        server.setEventListener(this);

        setJMenuBar(createMenuBar());
        userList.setCellRenderer(new UserListCellRenderer(true, server.getUsername()));

        userList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle cellBounds = userList.getCellBounds(index, index);
                    int clickX = e.getX() - cellBounds.x;

                    // Detect click near delete icon
                    if (clickX > cellBounds.width - 30) {
                        String username = userListModel.get(index);
                        int confirm = JOptionPane.showConfirmDialog(
                                userList,
                                "Kick out user: " + username + "?",
                                "Confirm Kick",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                server.kickUser(username);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(userList,
                                        "Failed to remove user: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onJoinRequest(String username) {
        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Allow user '" + username + "' to join?",
                    "New Join Request",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                server.approveClient(username);
            } else {
                server.denyClient(username);
            }
        });
    }

    // File operations menu
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem closeItem = new JMenuItem("Close");

        // Assign keyboard shortcuts
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveAsItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

        // Add listeners
        newItem.addActionListener(e -> onNewFile());
        openItem.addActionListener(e -> onOpenFile());
        saveItem.addActionListener(e -> onSaveFile());
        saveAsItem.addActionListener(e -> onSaveAsFile());
        closeItem.addActionListener(e -> onCloseFile());

        // Add to menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);

        menuBar.add(fileMenu);
        return menuBar;
    }

    // File operation handlers
    private void onNewFile() {
        System.out.println("[Server] New whiteboard created.");
        // Clear canvas, broadcast reset to clients

        // Reset to default file name
        whiteboardFile = new File("Whiteboard.png");

        canvas.clear();


        try {
            server.broadcastCanvasImage();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void onOpenFile() {
        System.out.println("[Server] Opening existing whiteboard...");
        JFileChooser chooser = new JFileChooser();
    
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                "Image Files (JPG, JPEG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
         chooser.setFileFilter(imageFilter);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // TODO: load saved whiteboard
            whiteboardFile = chooser.getSelectedFile();
            try {
                canvas.loadFromFile(whiteboardFile);
                server.broadcastCanvasImage();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void onSaveFile() {
        System.out.println("[Server] Saving current whiteboard...");
        // TODO: serialize whiteboard state
        try {
            canvas.saveCanvasToFile(whiteboardFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void onSaveAsFile() {
        System.out.println("[Server] Saving whiteboard as...");
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                "Image Files (JPG, JPEG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
         chooser.setFileFilter(imageFilter);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // TODO: write to selected file
            whiteboardFile = chooser.getSelectedFile();
            try {
                canvas.saveCanvasToFile(whiteboardFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void onCloseFile() {
        System.out.println("[Server] Closing current whiteboard...");
        dispose(); // Close the window
    }

}
