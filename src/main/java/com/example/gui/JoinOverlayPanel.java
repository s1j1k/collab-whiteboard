package com.example.gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class JoinOverlayPanel extends JPanel {
    private final JTextField usernameField;
    private final JButton joinButton;

    public JoinOverlayPanel(Consumer<String> onJoin) {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 150)); // semi-transparent dark gray

        JPanel inner = new JPanel();
        inner.setBackground(new Color(240, 240, 240));
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter your username:");
        usernameField = new JTextField(15);
        joinButton = new JButton("Join Whiteboard");

        joinButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) onJoin.accept(username);
        });

        inner.add(label);
        inner.add(Box.createVerticalStrut(10));
        inner.add(usernameField);
        inner.add(Box.createVerticalStrut(10));
        inner.add(joinButton);

        add(inner);
    }

    public void setEnabledState(boolean enabled) {
        usernameField.setEnabled(enabled);
        joinButton.setEnabled(enabled);
    }
}

