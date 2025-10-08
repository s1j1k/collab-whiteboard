package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolBarPanel extends JPanel {

    private JButton clearButton;
    private JButton colorButton;

    public ToolBarPanel(ActionListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        clearButton = new JButton("Clear");
        colorButton = new JButton("Color");

        clearButton.setActionCommand("CLEAR");
        colorButton.setActionCommand("COLOR");

        clearButton.addActionListener(listener);
        colorButton.addActionListener(listener);

        add(clearButton);
        add(colorButton);
    }
}
