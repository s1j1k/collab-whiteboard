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

        canvas = new WhiteBoardCanvas(800, 600);
        toolbar = new ToolBarPanel(canvas);

        add(toolbar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public WhiteBoardCanvas getCanvas() {
        return canvas;
    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
}
