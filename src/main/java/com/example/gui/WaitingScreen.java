package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Waiting screen for clients to attempt to join whiteboard
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class WaitingScreen extends JFrame {
    private final JTextField usernameField;
    private final JButton connectButton;
    private final JLabel messageLabel;
    private final JLabel spinnerLabel;

    public WaitingScreen(ActionListener onConnect) {
        // Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Join Whiteboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Join Whiteboard", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));

        // Username field
        usernameField = new JTextField(20);
        usernameField.putClientProperty("JComponent.roundRect", true);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");

        // Connect button
        connectButton = new JButton("Connect");
        connectButton.putClientProperty("JButton.buttonType", "roundRect");
        connectButton.setFont(connectButton.getFont().deriveFont(Font.BOLD));
        connectButton.addActionListener(onConnect);

        // Message and spinner
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(16f));
        messageLabel.setForeground(UIManager.getColor("Label.foreground"));

        spinnerLabel = new JLabel(new ImageIcon(getClass().getResource("/spinner.gif")), SwingConstants.CENTER);
        spinnerLabel.setVisible(false);

        // Center layout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);
        centerPanel.add(title, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(usernameField, gbc);

        gbc.gridy++;
        centerPanel.add(connectButton, gbc);

        gbc.gridy++;
        centerPanel.add(messageLabel, gbc);

        gbc.gridy++;
        centerPanel.add(spinnerLabel, gbc);

        content.add(centerPanel, BorderLayout.CENTER);
        setContentPane(content);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    /** Switch to waiting mode */
    public void showWaitingState() {
        usernameField.setEnabled(false);
        connectButton.setEnabled(false);
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setText("Waiting for server approval...");
        spinnerLabel.setVisible(true);
    }

    /** Show rejection reason */
    public void showRejection(String reason) {
        spinnerLabel.setVisible(false);
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(reason);
        usernameField.setEnabled(true);
        connectButton.setEnabled(true);
    }
}
