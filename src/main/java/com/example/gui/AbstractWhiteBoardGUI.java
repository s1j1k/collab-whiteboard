package com.example.gui;

import javax.swing.*;

import com.example.shared.Tool;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractWhiteBoardGUI extends JFrame implements ActionListener {

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

        canvas = new WhiteBoardCanvas(800, 600);
        toolbar = new ToolBarPanel(this);

        add(toolbar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        // TODO implement shapes
        try {
            // Try to parse Tool enum
            Tool selectedTool = Tool.valueOf(cmd);
            canvas.setCurrentTool(selectedTool);
        } catch (IllegalArgumentException ex) {
            // Not a tool, handle other commands
            if ("CLEAR".equals(cmd)) {
                canvas.clear();
                broadcastClear(); // implemented differently by client/server
            } else if ("COLOR".equals(cmd)) {
                Color newColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
                if (newColor != null) {
                    canvas.setDrawingColor(newColor);
                }
            }
            ;
        }
    }

    // These methods can be overridden by subclasses if needed
    protected abstract void broadcastClear();
}
