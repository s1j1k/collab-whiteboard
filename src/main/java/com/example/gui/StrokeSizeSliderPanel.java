package com.example.gui;
import javax.swing.*;
import java.awt.*;

public class StrokeSizeSliderPanel extends JPanel {
    private final JSlider strokeSizeSlider;
    private final JLabel strokeLabel;

    public StrokeSizeSliderPanel(int min, int max, int initial) {
        setLayout(new BorderLayout(5, 5));
        // setLayout(new FlowLayout(FlowLayout.LEFT));
        setOpaque(false);

        // Label above slider
        strokeLabel = new JLabel("Stroke Size", SwingConstants.CENTER);
        strokeLabel.setFont(strokeLabel.getFont().deriveFont(Font.BOLD, 12f));
        strokeLabel.setForeground(Color.BLACK);
        

        // Slider setup
        strokeSizeSlider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        strokeSizeSlider.setToolTipText("Stroke Size");
        strokeSizeSlider.setForeground(Color.BLACK);
        // strokeSizeSlider.setBackground(Color.LIGHT_GRAY);
 
        // ---- Layout ----
        add(strokeLabel, BorderLayout.NORTH);
        add(strokeSizeSlider, BorderLayout.CENTER);
    }

    public int getStrokeSize() {
        return strokeSizeSlider.getValue();
    }

    public JSlider getSlider() {
        return strokeSizeSlider;
    }

    // Example test window
    public static void main(String[] args) {

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
