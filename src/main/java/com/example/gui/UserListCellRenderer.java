package com.example.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Custom renderer to add option of deleting to User List view
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class UserListCellRenderer extends JPanel implements ListCellRenderer<String> {
    private final JLabel nameLabel = new JLabel();
    private final JLabel deleteIcon = new JLabel();
    private final boolean showKickIcons;
    private String hostUsername;

    public UserListCellRenderer(boolean showKickIcons) {
        this.showKickIcons = showKickIcons;
        setLayout(new BorderLayout(5, 0));
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(nameLabel, BorderLayout.CENTER);

        if (showKickIcons) {
            deleteIcon.setIcon(IconUtils.loadScaledIcon("/icons/delete.png", 24, 24));
            add(deleteIcon, BorderLayout.EAST);
        }
        setOpaque(true);
    }

    public UserListCellRenderer(boolean showKickIcons, String hostUsername) {
        this(showKickIcons);
        this.hostUsername = hostUsername;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends String> list, String value, int index,
            boolean isSelected, boolean cellHasFocus) {

        nameLabel.setText(value);

        if (isSelected) {
            setBackground(new Color(220, 220, 220));
        } else {
            setBackground(Color.WHITE);
        }

        // Only show delete for whiteboard host
        if (showKickIcons && !value.equals(hostUsername)) {
            deleteIcon.setVisible(true);
        } else {
            deleteIcon.setVisible(false);
        }

        return this;
    }
}
