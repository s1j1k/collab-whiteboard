package com.example.gui;

import javax.swing.*;
import java.awt.*;

import com.example.shared.Tool;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * 
 * Whiteboard tool bar
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class ToolBarPanel extends JPanel {

    private JButton colorButton;
    private Color currentColor = Color.BLACK;

    public ToolBarPanel(WhiteBoardCanvas canvas) {
        // FlatLaf setup
        try {
            FlatLightLaf.setup();
        } catch (Exception ignored) {
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        // Left section: tools
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);
        // Toolbar for tool buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();
        // Make toolbar not floatable
        toolBar.setFloatable(false);

        // Stroke size label and slider
        StrokeSizeSliderPanel strokeSizeSliderPanel = new StrokeSizeSliderPanel(1, 50, 5);
        JSlider strokeSizeSlider = strokeSizeSliderPanel.getSlider();

        // Dynamic stroke preview
        JPanel strokePreview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int s = strokeSizeSlider.getValue();
                // Check if eraser
                if (canvas.getCurrentTool() == Tool.ERASE) {
                    g2.setColor(Color.BLACK);
                    g2.drawOval(getWidth() / 2 - s / 2, getHeight() / 2 - s / 2, s, s);
                } else {
                    g2.setColor(currentColor);
                    g2.fillOval(getWidth() / 2 - s / 2, getHeight() / 2 - s / 2, s, s);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 40);
            }
        };
        strokePreview.setOpaque(false);

        strokeSizeSlider.addChangeListener(e -> {
            if (canvas.getCurrentTool() == Tool.ERASE) {
                canvas.setEraserStrokeSize(strokeSizeSlider.getValue());
            } else {
                canvas.setDrawStrokeSize(strokeSizeSlider.getValue());
            }

            strokePreview.repaint();
        });

        // Create toggle buttons for each tool
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());

            btn.addActionListener(e -> {
                canvas.setCurrentTool(t);

                if (t == Tool.ERASE) {
                    strokeSizeSlider.setValue(canvas.getEraserStrokeSize());
                } else {
                    strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
                }
                // Refresh preview
                strokePreview.repaint();

            });

            // Select a default button
            if (t == Tool.LINE) {
                btn.setSelected(true);
                canvas.setCurrentTool(t);
                strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
            }

            buttonGroup.add(btn);
            toolBar.add(btn);
        }

        // Color button
        colorButton = new JButton(IconUtils.loadScaledIcon("/icons/colors.png", 24, 24));
        colorButton.setToolTipText("Color");
        colorButton.setFocusable(false);
        colorButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // keep spacing
        colorButton.setContentAreaFilled(false);
        colorButton.setOpaque(false); 
        colorButton.setRolloverEnabled(true);
        // Make hoverable
        colorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                colorButton.setContentAreaFilled(true); 
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                colorButton.setContentAreaFilled(false);
            }
        });
        colorButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose Drawing Color", currentColor);
            if (chosen != null) {
                currentColor = chosen; 
                canvas.setDrawingColor(chosen); 
                strokePreview.repaint();
            }
        });

        // Add to layout
        leftPanel.add(toolBar);
        leftPanel.add(strokeSizeSliderPanel);
        leftPanel.add(strokePreview);
        leftPanel.add(colorButton);
        // Add to main panel
        add(leftPanel, BorderLayout.WEST);
    }
}
