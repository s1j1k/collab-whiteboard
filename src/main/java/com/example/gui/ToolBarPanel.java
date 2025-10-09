package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.example.shared.Tool;

public class ToolBarPanel extends JPanel {

    private JButton shapesButton;
    private JButton clearButton;
    private JButton colorButton;

    public ToolBarPanel(ActionListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // TODO Shapes: your whiteboard should at least support line, triangle, circle,
        // and rectangle.

        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();

        // Create toggle buttons for each tool
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());

            // Set the enum name as the action command
            btn.setActionCommand(t.name());

            // Use the passed-in listener
            btn.addActionListener(listener);
            // TODO change the current tool based on this
            // FIXME remove
            // btn.addActionListener(e -> canvas.setCurrentTool(t));
            toolBar.add(btn);
            buttonGroup.add(btn);
        }

        // TODO add object selection or move items (hand icon)
        // TODO add eraser

        add(toolBar);

        // Add action listeners
        // lineBtn.addActionListener(e -> canvas.setCurrentTool(Tool.LINE));
        // rectBtn.addActionListener(e -> canvas.setCurrentTool(Tool.RECTANGLE));
        // ovalBtn.addActionListener(e -> canvas.setCurrentTool(Tool.OVAL));
        // freeDrawBtn.addActionListener(e -> canvas.setCurrentTool(Tool.FREE_DRAW));
        // textBtn.addActionListener(e -> canvas.setCurrentTool(Tool.TEXT));

        // TODO add button for free drawing
        // TODO add icons for buttons
        // ImageIcon icon = new ImageIcon("path/to/myIcon.png");
        // JButton shapesButton = new
        // JButton(IconUtils.loadScaledIcon("/icons/icons8-shapes-64.png", 24, 24));
        // ImageIcon pencilIcon = new ImageIcon(
        // getClass().getResource("/icons/pencil.png")
        // );

        // FIXME add alt text for accessibility
        // TODO add tooltip text instead of text labels

        clearButton = new JButton("Clear", IconUtils.loadScaledIcon("/icons/icons8-clear-50.png", 15, 15));
        colorButton = new JButton("Color", IconUtils.loadScaledIcon("/icons/colors.png", 15, 15));

        clearButton.setActionCommand("CLEAR");
        colorButton.setActionCommand("COLOR");

        clearButton.addActionListener(listener);
        colorButton.addActionListener(listener);

        // FIXME do this to all
        // shapesButton.setFocusable(false);
        // shapesButton.setBorderPainted(false);
        // shapesButton.setContentAreaFilled(false);
        // shapesButton.setOpaque(false);

        // add(shapesButton);
        add(clearButton);
        add(colorButton);
    }
}
