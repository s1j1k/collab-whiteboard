package com.example.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 
 * Stroke size slider
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class StrokeSizeSliderPanel extends JPanel {
    private final JSlider strokeSizeSlider;
    private final JLabel strokeLabel;

    public StrokeSizeSliderPanel(int min, int max, int initial) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        // Label above slider
        strokeLabel = new JLabel("Stroke Size", SwingConstants.CENTER);
        strokeLabel.setFont(strokeLabel.getFont().deriveFont(Font.BOLD, 12f));
        strokeLabel.setForeground(Color.BLACK);

        // Slider setup
        strokeSizeSlider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        strokeSizeSlider.setToolTipText("Stroke Size");
        strokeSizeSlider.setForeground(Color.BLACK);

        // Layout
        add(strokeLabel, BorderLayout.NORTH);
        add(strokeSizeSlider, BorderLayout.CENTER);
    }

    public int getStrokeSize() {
        return strokeSizeSlider.getValue();
    }

    public JSlider getSlider() {
        return strokeSizeSlider;
    }
}
