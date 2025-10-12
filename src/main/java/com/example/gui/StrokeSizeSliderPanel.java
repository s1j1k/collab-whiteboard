package com.example.gui;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class StrokeSizeSliderPanel extends JPanel {
    private final JSlider strokeSizeSlider;
    private final JLabel strokeLabel;

    public StrokeSizeSliderPanel(int min, int max, int initial) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        // Label above slider
        strokeLabel = new JLabel("Stroke Size", SwingConstants.CENTER);
        add(strokeLabel, BorderLayout.NORTH);

        // Slider setup
        strokeSizeSlider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        strokeSizeSlider.setOpaque(false);
        strokeSizeSlider.setPaintTicks(false);
        strokeSizeSlider.setPaintLabels(false);

        // FlatLaf slider styling
        strokeSizeSlider.putClientProperty("JSlider.trackWidth", 6);
        strokeSizeSlider.putClientProperty("JSlider.trackColor", Color.GRAY);
        strokeSizeSlider.putClientProperty("JSlider.thumbColor", Color.BLACK);

        // Initial thumb size
        updateThumbSize(initial);

        // Dynamically change thumb size when value changes
        strokeSizeSlider.addChangeListener(e -> {
            int size = strokeSizeSlider.getValue();
            updateThumbSize(size);
        });

        add(strokeSizeSlider, BorderLayout.CENTER);
    }

    private void updateThumbSize(int size) {
        // Thumb size grows with stroke size (but not too large)
        int thumbSize = Math.min(30, Math.max(10, size + 5));
        strokeSizeSlider.putClientProperty("JSlider.thumbSize", new Dimension(thumbSize, thumbSize));
        strokeSizeSlider.repaint();
    }

    public int getStrokeSize() {
        return strokeSizeSlider.getValue();
    }

    public JSlider getSlider() {
        return strokeSizeSlider;
    }

    // Example test window
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Stroke Size Slider");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new StrokeSizeSliderPanel(1, 50, 5), BorderLayout.CENTER);
            frame.setSize(300, 120);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
