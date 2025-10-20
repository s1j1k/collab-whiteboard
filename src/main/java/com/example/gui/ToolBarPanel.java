package com.example.gui;

import javax.swing.*;
import java.awt.*;

import com.example.shared.Tool;
import com.formdev.flatlaf.FlatLightLaf;

// TODO allow text resize

public class ToolBarPanel extends JPanel {

    private JButton colorButton;
    private Color currentColor = Color.BLACK;

    public ToolBarPanel(WhiteBoardCanvas canvas) {
        // FlatLaf setup (optional if already initialized in main)
        try {
            FlatLightLaf.setup();
        } catch (Exception ignored) {
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        // === Left section: tools ===
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);
        // === Toolbar for tool buttons ===
        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();
        // FIXME make it floatable?
        toolBar.setFloatable(false);

        // === Right section: stroke size + color ===
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);
        // === Stroke size label + slider ===
        StrokeSizeSliderPanel strokeSizeSliderPanel = new StrokeSizeSliderPanel(1, 50, 5);
        JSlider strokeSizeSlider = strokeSizeSliderPanel.getSlider();

        // TODO place this on the left
        // ---- Dynamic Stroke Preview ----
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

        // TODO make it change color based on tool
        strokeSizeSlider.addChangeListener(e -> {
            if (canvas.getCurrentTool() == Tool.ERASE) {
                canvas.setEraserStrokeSize(strokeSizeSlider.getValue());
            } else {
                canvas.setDrawStrokeSize(strokeSizeSlider.getValue());
            }

            strokePreview.repaint();
        });

        // === Create toggle buttons for each tool ===
        // FIXME toggle buttons not holding down
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());
            // btn.setFocusable(false);
            // btn.setContentAreaFilled(false);
            // btn.setBorderPainted(false);
            // btn.setOpaque(false);

            btn.addActionListener(e -> {
                canvas.setCurrentTool(t);

                // TODO change stroke size preview color
                if (t == Tool.ERASE) {
                    strokeSizeSlider.setValue(canvas.getEraserStrokeSize());
                } else {
                    strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
                }

                strokePreview.repaint(); // refresh preview

            });

            // Select a default button
            // TODO change to select tool
            if (t == Tool.LINE) {
                btn.setSelected(true);
                canvas.setCurrentTool(t);
                strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
            }

            buttonGroup.add(btn);
            toolBar.add(btn);
        }

        

        // === Color Button ===
        colorButton = new JButton(IconUtils.loadScaledIcon("/icons/colors.png", 24, 24));
        colorButton.setToolTipText("Color");
        colorButton.setFocusable(false);
        colorButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // keep spacing
        colorButton.setContentAreaFilled(false); // remove default background fill
        colorButton.setOpaque(false); // allow look-and-feel to show hover state
        colorButton.setRolloverEnabled(true); // ensure hover effect works
        colorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                colorButton.setContentAreaFilled(true); // show highlight
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                colorButton.setContentAreaFilled(false); // revert highlight
            }
        });
        colorButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose Drawing Color", currentColor);
            if (chosen != null) {
                currentColor = chosen; // update preview color
                canvas.setDrawingColor(chosen); // update canvas color
                strokePreview.repaint(); // refresh preview
            }
        });

        // === Add to layout ===
        leftPanel.add(toolBar);
        // TODO add a little space here
        leftPanel.add(strokeSizeSliderPanel);
        leftPanel.add(strokePreview);
        leftPanel.add(colorButton);
        // === Add both to main panel ===
        add(leftPanel, BorderLayout.WEST);
        // add(rightPanel, BorderLayout.CENTER);

        // === TODOs (kept from your version) ===
        // TODO Shapes: your whiteboard should at least support line, triangle, circle,
        // and rectangle.
        // TODO improvement: make this appear only if a drawing tool is selected
        // TODO add object selection or move items (hand icon)
        // TODO add eraser
        // TODO show a preview of stroke size where the cursor is while scrolling the
        // bar
        // TODO improve architecture (action command)
    }
}
