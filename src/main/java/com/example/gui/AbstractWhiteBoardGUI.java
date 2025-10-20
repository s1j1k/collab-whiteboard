package com.example.gui;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;

public abstract class AbstractWhiteBoardGUI extends JFrame {

    protected WhiteBoardCanvas canvas;
    protected ToolBarPanel toolbar;
    protected ChatPanel chatPanel;

    // User list filds
    private JToggleButton usersButton;
    protected JList<String> userList;
    protected DefaultListModel<String> userListModel;
    protected JScrollPane userScrollPane;

    public AbstractWhiteBoardGUI(String title) {
        super(title);

        // Install FlatLaf look and feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new WhiteBoardCanvas(800, 600);
        toolbar = new ToolBarPanel(canvas);

        add(toolbar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        chatPanel = new ChatPanel();
        add(chatPanel, BorderLayout.WEST);


        // === Users Button ===
        usersButton = new JToggleButton(IconUtils.loadScaledIcon("/icons/users.png", 24, 24));
        usersButton.setToolTipText("Show connected users");
        usersButton.addActionListener(e -> toggleUserPanel());
        usersButton.setFocusable(false);
        usersButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // keep spacing
        usersButton.setContentAreaFilled(false); // remove default background fill
        usersButton.setOpaque(false);
        usersButton.setRolloverEnabled(true); // ensure hover effect works
        toolbar.add(usersButton, BorderLayout.EAST);

        // === Users Panel ===
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(180, 0));
        userScrollPane.setVisible(false);
        add(userScrollPane, BorderLayout.EAST);

        // Modern UI with Flatlaf
        userList.putClientProperty("JList.roundRectSelection", true);
        userList.putClientProperty("JList.showCellFocusIndicator", false);

        pack();
        setLocationRelativeTo(null);
    }

    public WhiteBoardCanvas getCanvas() {
        return canvas;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    protected void toggleUserPanel() {
        userScrollPane.setVisible(!userScrollPane.isVisible());
        revalidate();
        repaint();
    }

    public void updateUserList(java.util.List<String> usernames) {
        userListModel.clear();
        for (String name : usernames) {
            userListModel.addElement(name);
        }
    }

}
