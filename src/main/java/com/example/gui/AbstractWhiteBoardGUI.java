package com.example.gui;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;

public abstract class AbstractWhiteBoardGUI extends JFrame {

    protected WhiteBoardCanvas canvas;
    protected ToolBarPanel toolbar;
    protected ChatPanel chatPanel;

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

        chatPanel = new ChatPanel();
        // chatPanel.setChatListener(message -> {
        //     // send message to server via RMI later
        //     System.out.println("Sending: " + message);
        // });

        // Add it to your layout (e.g., right side of canvas)
        add(chatPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    public WhiteBoardCanvas getCanvas() {
        return canvas;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }
}
