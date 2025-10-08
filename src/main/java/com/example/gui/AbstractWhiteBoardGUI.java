package com.example.gui;

import javax.swing.*;

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
        switch (e.getActionCommand()) {
            case "CLEAR":
                canvas.clear();
                broadcastClear(); // implemented differently by client/server
                break;
            case "COLOR":
                Color newColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
                if (newColor != null) {
                    canvas.setDrawingColor(newColor);
                }
                break;
        }
    }

    // These methods can be overridden by subclasses if needed
    protected abstract void broadcastClear();
}
