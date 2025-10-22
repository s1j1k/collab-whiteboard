package com.example.gui;

import com.example.shared.ChatListener;
import com.example.shared.ChatMessage;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Chat window
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class ChatPanel extends JPanel {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private ChatListener listener;

    public ChatPanel() {

        // Install FlatLaf look and feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e);
        }

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Chat"));

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField(10);
        inputField.putClientProperty("JComponent.roundRect", true);

        sendButton = new JButton("Send");
        sendButton.putClientProperty("JButton.buttonType", "roundRect");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Events
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && listener != null) {
            listener.onMessageSend(text);
            inputField.setText("");
        }
    }

    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void setChatListener(ChatListener listener) {
        this.listener = listener;
    }

    public void loadHistory(ArrayList<ChatMessage> history) {
        for (ChatMessage msg : history) {
            appendMessage(msg.toString());
        }
    }

}
