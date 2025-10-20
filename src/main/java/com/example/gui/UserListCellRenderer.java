package com.example.gui;

import javax.swing.*;
import java.awt.*;

public class UserListCellRenderer extends JPanel implements ListCellRenderer<String> {
    private final JLabel nameLabel = new JLabel();
    private final JLabel deleteIcon = new JLabel();
    private final boolean showKickIcons;

    public UserListCellRenderer(boolean showKickIcons) {
        this.showKickIcons = showKickIcons;
        setLayout(new BorderLayout(5, 0));
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(nameLabel, BorderLayout.CENTER);

        if (showKickIcons) {
            deleteIcon.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
            add(deleteIcon, BorderLayout.EAST);
        }
        setOpaque(true);
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

        return this;
    }
}
