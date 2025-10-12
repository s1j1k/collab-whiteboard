package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import com.example.shared.IconUtils;
import com.example.shared.Tool;

public class ToolBarPanel extends JPanel {

    private JButton shapesButton;
    private JButton clearButton;
    private JButton colorButton;

    public ToolBarPanel(ActionListener listener, WhiteBoardCanvas canvas) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // TODO Shapes: your whiteboard should at least support line, triangle, circle,
        // and rectangle.

        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();

        // Create toggle buttons for each tool
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());
            // FIXME remove lambda
            // Change current tool based on selection
            btn.addActionListener(e -> canvas.setCurrentTool(t));
            toolBar.add(btn);
            buttonGroup.add(btn);
        }

        // TODO add object selection or move items (hand icon)
        // TODO add eraser

        add(toolBar);

        // TODO improvement: make this appear only if a drawing tool is selected
        JSlider strokeSizeSlider = new JSlider(1, 50, 2);
        JLabel strokeLabel = new JLabel("Stroke Size");

        add(strokeSizeSlider);
        add(strokeLabel);

        strokeSizeSlider.addChangeListener(e -> {
            if (canvas.getCurrentTool() == Tool.ERASE) {
                canvas.setEraserStrokeSize(strokeSizeSlider.getValue());
            } else {
                // FIXME this assumes there is only erase or draw tool, not select
                // TODO check if it's a drawing tool
                canvas.setDrawStrokeSize(strokeSizeSlider.getValue());
            }
        });

        // FIXME action command is a bit clunky
        // TODO improve architecture
        colorButton = new JButton(IconUtils.loadScaledIcon("/icons/colors.png", 15, 15));
        
        // FIXME clear feature not required
        // clearButton = new JButton("Clear", IconUtils.loadScaledIcon("/icons/icons8-clear-50.png", 15, 15));

        colorButton.setActionCommand("COLOR");

        // clearButton.setActionCommand("CLEAR");

        colorButton.addActionListener(listener);
        // clearButton.addActionListener(listener);

        // FIXME do this to all
        // shapesButton.setFocusable(false);
        // shapesButton.setBorderPainted(false);
        // shapesButton.setContentAreaFilled(false);
        // shapesButton.setOpaque(false);

        // add(shapesButton);
        // add(clearButton);
        add(colorButton);
    }
}
