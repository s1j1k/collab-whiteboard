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

        // TODO Shapes: your whiteboard should at least support line, triangle, circle, and rectangle.

        ButtonGroup buttonGroup = new ButtonGroup();
        JToolBar toolBar = new JToolBar();

        // ---- Stroke Size Label ----
        JLabel strokeLabel = new JLabel("Stroke Size");
        strokeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        strokeLabel.setForeground(Color.BLACK);
        strokeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- Stroke Size Slider ----
        JSlider strokeSizeSlider = new JSlider(1, 50, 2);
        strokeSizeSlider.setToolTipText("Stroke Size");
        strokeSizeSlider.setForeground(Color.BLACK);
        strokeSizeSlider.setBackground(Color.LIGHT_GRAY);

        // ---- Custom UI to resize the thumb ----
        strokeSizeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(strokeSizeSlider) {
            @Override
            protected Dimension getThumbSize() {
                int size = strokeSizeSlider.getValue(); // use stroke size as thumb size
                int min = 10;
                int max = 30;
                int scaled = Math.min(max, Math.max(min, size / 2)); // clamp for usability
                return new Dimension(scaled, scaled);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (canvas.getCurrentTool() == Tool.ERASE) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawOval(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                }
                g2.dispose();
            }
        });

        // ---- Dynamic Stroke Preview ----
        // JPanel strokePreview = new JPanel() {
        //     @Override
        //     protected void paintComponent(Graphics g) {
        //         super.paintComponent(g);
        //         Graphics2D g2 = (Graphics2D) g;
        //         if (canvas.getCurrentTool() == Tool.ERASE) {
        //             g2.setColor(Color.WHITE);
        //             g2.setStroke(new BasicStroke(1));
        //             g2.drawOval(getWidth() / 2 - strokeSizeSlider.getValue() / 2,
        //                     getHeight() / 2 - strokeSizeSlider.getValue() / 2,
        //                     strokeSizeSlider.getValue(), strokeSizeSlider.getValue());
        //         } else {
        //             g2.setColor(Color.BLACK);
        //             g2.fillOval(getWidth() / 2 - strokeSizeSlider.getValue() / 2,
        //                     getHeight() / 2 - strokeSizeSlider.getValue() / 2,
        //                     strokeSizeSlider.getValue(), strokeSizeSlider.getValue());
        //         }
        //     }

        //     @Override
        //     public Dimension getPreferredSize() {
        //         return new Dimension(60, 40);
        //     }
        // // };
        // strokePreview.setBackground(Color.LIGHT_GRAY);
        // strokePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // ---- Toolbar Buttons ----
        for (Tool t : Tool.values()) {
            JToggleButton btn = new JToggleButton(IconUtils.loadScaledIcon(t.getIconPath(), 24, 24));
            btn.setToolTipText(t.getDisplayName());
            // Change current tool based on selection
            btn.addActionListener(e -> {
                canvas.setCurrentTool(t);
                if (t == Tool.ERASE) {
                    strokeSizeSlider.setValue(canvas.getEraserStrokeSize());
                } else {
                    strokeSizeSlider.setValue(canvas.getDrawStrokeSize());
                }
                strokePreview.repaint();
                strokeSizeSlider.repaint();
            });

            toolBar.add(btn);
            buttonGroup.add(btn);
        }

        add(toolBar);

        // ---- Stroke Control Section ----
        JPanel strokePanel = new JPanel();
        strokePanel.setLayout(new BoxLayout(strokePanel, BoxLayout.Y_AXIS));
        strokePanel.add(strokeLabel);
        strokePanel.add(strokePreview);
        strokePanel.add(strokeSizeSlider);
        add(strokePanel);

        // ---- Change Listener for Stroke Size ----
        strokeSizeSlider.addChangeListener(e -> {
            if (canvas.getCurrentTool() == Tool.ERASE) {
                canvas.setEraserStrokeSize(strokeSizeSlider.getValue());
            } else {
                canvas.setDrawStrokeSize(strokeSizeSlider.getValue());
            }
            strokePreview.repaint();
            strokeSizeSlider.repaint(); // refresh thumb size
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
