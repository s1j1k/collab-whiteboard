package com.example.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ServerGUI extends AbstractWhiteBoardGUI {

    // GUI Components
    // WhiteBoardCanvas canvas = new WhiteBoardCanvas(500, 500);

    public ServerGUI() {
        super("Server Whiteboard");
        setJMenuBar(createMenuBar());
    }

    // @Override
    // protected void broadcastClear() {
    // // TODO send a “clear” message to all clients
    // }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // === FILE MENU ===
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

        // Add listeners (placeholders for now)
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

    // === Placeholder Handlers (manager-controlled) ===
    private void onNewFile() {
        System.out.println("[Server] New whiteboard created.");
        // TODO: clear canvas, broadcast reset to clients
    }

    private void onOpenFile() {
        System.out.println("[Server] Opening existing whiteboard...");
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // TODO: load saved whiteboard
        }
    }

    private void onSaveFile() {
        System.out.println("[Server] Saving current whiteboard...");
        // TODO: serialize whiteboard state
    }

    private void onSaveAsFile() {
        System.out.println("[Server] Saving whiteboard as...");
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // TODO: write to selected file
        }
    }

    private void onCloseFile() {
        System.out.println("[Server] Closing current whiteboard...");
        dispose(); // Close the window
    }

}
