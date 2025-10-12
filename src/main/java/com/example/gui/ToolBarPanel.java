package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import com.example.shared.IconUtils;
import com.example.shared.Tool;
import com.formdev.flatlaf.FlatLightLaf;

public class ToolBarPanel extends JPanel {

    private JButton colorButton;

    public ToolBarPanel(ActionListener listener, WhiteBoardCanvas canvas) {
        // FlatLaf setup (optional if already initialized in main)
        try {
            FlatLightLaf.setup();
        } catch (Exception ignored) {}

        setLayout(new FlowLayout(FlowLayout.LEFT));
        setOpaque(false);

        // === Toolbar for tool buttons ===
        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // === Stroke size label + slider ===
        JLabel strokeLabel = new JLabel("Stroke Size");
        strokeLabel.setFont(strokeLabel.getFont().deriveFont(Font.BOLD, 12f));
        strokeLabel.setForeground(Color.BLACK);

        JSlider strokeSizeSlider = new JSlider(1, 50, 2);
        strokeSizeSlider.setToolTipText("Adjust stroke size");
        strokeSizeSlider.setOpaque(false);
        strokeSizeSlider.putClientProperty("JSlider.trackWidth", 6);
        strokeSizeSlider.putClientProperty("JSlider.trackColor", Color.GRAY);
        strokeSizeSlider.putClientProperty("JSlider.thumbColor", Color.BLACK);

        // Function to update thumb size dynamically
        Runnable updateThumbSize = () -> {
            int size = strokeSizeSlider.getValue();
            int thumbSize = Math.min(30, Math.max(10, size + 5));
            strokeSizeSlider.putClientProperty("JSlider.thumbSize", new Dimension(thumbSize, thumbSize));
            strokeSizeSlider.repaint();
        };
        updateThumbSize.run();

        strokeSizeSlider.addChangeListener(e -> {
            updateThumbSize.run();

            if (canvas.getCurrentTool() == Tool.ERASE) {
                canvas.setEraserStrokeSize(strokeSizeSlider.getValue());
            } else {
                canvas.setDrawStrokeSize(strokeSizeSlider.getValue());
            }
        });

        // === Create toggle buttons for each tool ===
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());
            btn.setFocusable(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setOpaque(false);

            btn.addActionListener(e -> {
                canvas.setCurrentTool(t);

                if (t == Tool.ERASE) {
                    strokeSizeSlider.setValue(canvas.getEraserStrokeSize());
                } else {
                    strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
                }
            });

            buttonGroup.add(btn);
            toolBar.add(btn);
        }

        // === Color Button ===
        colorButton = new JButton(IconUtils.loadScaledIcon("/icons/colors.png", 24, 24));
        colorButton.setActionCommand("COLOR");
        colorButton.addActionListener(listener);
        colorButton.setFocusable(false);
        colorButton.setContentAreaFilled(false);
        colorButton.setBorderPainted(false);
        colorButton.setOpaque(false);

        // === Add to layout ===
        add(toolBar);
        add(strokeLabel);
        add(strokeSizeSlider);
        add(colorButton);

        // === TODOs (kept from your version) ===
        // TODO Shapes: your whiteboard should at least support line, triangle, circle, and rectangle.
        // TODO improvement: make this appear only if a drawing tool is selected
        // TODO add object selection or move items (hand icon)
        // TODO add eraser
        // TODO show a preview of stroke size where the cursor is while scrolling the bar
        // TODO improve architecture (action command)
    }
}
