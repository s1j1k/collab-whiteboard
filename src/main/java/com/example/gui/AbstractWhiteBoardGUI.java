package com.example.gui;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;

public abstract class AbstractWhiteBoardGUI extends JFrame {

    protected WhiteBoardCanvas canvas;
    protected ToolBarPanel toolbar;

    public AbstractWhiteBoardGUI(String title) {
        super(title);

        // Install FlatLaf look and feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TODO make the whiteboard GUI as the listener and 
        // add callbacks in here to send out to network layer
        // implemented / concrete classes will have client/server to send the messages
        // TODO interface for sending the objects
        // TODO allow select by object
        canvas = new WhiteBoardCanvas(800, 600);
        toolbar = new ToolBarPanel(canvas);

        add(toolbar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
    //     String cmd = e.getActionCommand();
    //     // TODO implement shapes
    //     try {
    //         // Try to parse Tool enum
    //         Tool selectedTool = Tool.valueOf(cmd);
    //         canvas.setCurrentTool(selectedTool);
    //     } catch (IllegalArgumentException ex) {
            // Not a tool, handle other commands
            // FIXME clear is not required
            // if ("CLEAR".equals(cmd)) {
            //     canvas.clear();
            //     broadcastClear(); // implemented differently by client/server
            // } else 
            // if ("COLOR".equals(cmd)) {
            //     Color newColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
            //     if (newColor != null) {
            //         canvas.setDrawingColor(newColor);
            //     }
            // }
    //         // ;
    //     }
    // }

    // TODO include methods to be overridden here

    // These methods can be overridden by subclasses if needed
    // protected abstract void broadcastClear();
}
