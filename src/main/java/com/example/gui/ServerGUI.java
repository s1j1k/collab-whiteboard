package com.example.gui;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

/**
 * 
 * Server GUI
 * 
 * @author Sally Arnold
 */
public class ServerGUI extends AbstractWhiteBoardGUI implements ServerEventListener {

    // Default file name
    private File whiteboardFile = new File("Whiteboard.png");

    WhiteBoardServer server;

    // Set by any draw action since the last save, open or new; read on the EDT
    private boolean unsavedChanges = false;

    public ServerGUI(WhiteBoardServer server) {
        super("Server Whiteboard");

        this.server = server;
        server.setEventListener(this);

        setJMenuBar(createMenuBar());

        // Ask before discarding unsaved work when the window is closed
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCloseFile();
            }
        });
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
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                server.approveClient(username);
            } else {
                server.denyClient(username, "Denied by whiteboard host.");
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

    /** Called on the EDT whenever the canvas changes. */
    public void markModified() {
        unsavedChanges = true;
    }

    /**
     * If there are unsaved changes, offer to save them.
     *
     * @return true if it is fine to continue (saved, or changes discarded)
     */
    private boolean confirmDiscardUnsaved() {
        if (!unsavedChanges) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Save changes to " + whiteboardFile.getName() + "?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            return onSaveFile();
        }
        return choice == JOptionPane.NO_OPTION;
    }

    private void showError(String message, Exception e) {
        System.err.println("[ERROR] " + message + " " + e.getMessage());
        JOptionPane.showMessageDialog(this, message + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JFileChooser imageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (JPG, JPEG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        return chooser;
    }

    private void broadcastCanvas() {
        try {
            server.broadcastCanvasImage();
        } catch (RemoteException e) {
            showError("Failed to send the whiteboard to clients.", e);
        }
    }

    // File operation handlers
    private void onNewFile() {
        if (!confirmDiscardUnsaved()) {
            return;
        }
        whiteboardFile = new File("Whiteboard.png");
        canvas.clear();
        unsavedChanges = false;
        broadcastCanvas();
        System.out.println("New whiteboard created.");
    }

    private void onOpenFile() {
        if (!confirmDiscardUnsaved()) {
            return;
        }
        JFileChooser chooser = imageChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            canvas.loadFromFile(file);
        } catch (IOException e) {
            showError("Could not open " + file.getName() + ".", e);
            return;
        }
        whiteboardFile = file;
        unsavedChanges = false;
        broadcastCanvas();
    }

    /** @return true if the whiteboard was saved */
    private boolean onSaveFile() {
        try {
            canvas.saveCanvasToFile(whiteboardFile);
        } catch (IOException e) {
            showError("Could not save " + whiteboardFile.getName() + ".", e);
            return false;
        }
        unsavedChanges = false;
        System.out.println("Saved whiteboard to " + whiteboardFile);
        return true;
    }

    private boolean onSaveAsFile() {
        JFileChooser chooser = imageChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File file = chooser.getSelectedFile();
        // The canvas is always written as PNG
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }
        whiteboardFile = file;
        return onSaveFile();
    }

    private void onCloseFile() {
        if (!confirmDiscardUnsaved()) {
            return;
        }
        System.out.println("Closing whiteboard...");
        // The shutdown hook notifies clients before the JVM exits
        System.exit(0);
    }

}
