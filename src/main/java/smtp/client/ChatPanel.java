package smtp.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * ChatPanel uses the control DataOutputStream (passed from MainFrame) to send PM commands.
 * Modern Messenger-like UI with bubble messages, colors, and emoji support.
 */
public class ChatPanel extends JPanel {

    private final String clientName;
    private final DataOutputStream controlOut;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> clientList = new JList<>(model);
    private final JTextPane chatPane = new JTextPane();
    private final StyledDocument doc;
    private final JTextField input = new JTextField();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // Color scheme inspired by Messenger
    private final Color SENT_BUBBLE_COLOR = new Color(0, 132, 255); // Blue
    private final Color RECEIVED_BUBBLE_COLOR = new Color(240, 240, 240); // Light gray
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Color INPUT_BACKGROUND = new Color(245, 245, 245);

    public ChatPanel(String clientName, DataOutputStream controlOut) {
        this.clientName = clientName;
        this.controlOut = controlOut;
        this.doc = chatPane.getStyledDocument();

        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left panel - Online users
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JLabel onlineLabel = new JLabel("Online Users");
        onlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        onlineLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        leftPanel.add(onlineLabel, BorderLayout.NORTH);

        clientList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setCellRenderer(new UserListRenderer());
        JScrollPane userScroll = new JScrollPane(clientList);
        userScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        leftPanel.add(userScroll, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(200, 0));
        add(leftPanel, BorderLayout.WEST);

        // Center panel - Chat messages
        chatPane.setEditable(false);
        chatPane.setBackground(BACKGROUND_COLOR);
        chatPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(chatScroll, BorderLayout.CENTER);

        // Bottom panel - Input area
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setBackground(INPUT_BACKGROUND);
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        bottomPanel.add(input, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendBtn.setBackground(SENT_BUBBLE_COLOR);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendToSelected());
        input.addActionListener(e -> sendToSelected());
    }

    // Custom renderer for user list with colored dots
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText("● " + value);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if (isSelected) {
                label.setBackground(new Color(230, 240, 255));
                label.setForeground(SENT_BUBBLE_COLOR);
            } else {
                label.setForeground(new Color(34, 139, 34)); // Green dot
            }
            return label;
        }
    }

    public void updateClientList(String[] clients) {
        SwingUtilities.invokeLater(() -> {
            model.clear();
            Arrays.stream(clients)
                .filter(s -> s != null && !s.isEmpty() && !s.equals(clientName))
                .forEach(model::addElement);
        });
    }

    public void onPrivateMessage(String from, String message) {
        SwingUtilities.invokeLater(() -> appendMessage(from, message, false));
    }

    private void appendMessage(String username, String message, boolean isSent) {
        try {
            String time = timeFormat.format(new Date());
            
            // Add spacing between messages
            doc.insertString(doc.getLength(), "\n", null);
            
            // Create message bubble style
            Style bubbleStyle = chatPane.addStyle("Bubble", null);
            StyleConstants.setBackground(bubbleStyle, isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
            StyleConstants.setForeground(bubbleStyle, isSent ? Color.WHITE : Color.BLACK);
            StyleConstants.setAlignment(bubbleStyle, isSent ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            
            // Username and time header
            Style headerStyle = chatPane.addStyle("Header", null);
            StyleConstants.setFontSize(headerStyle, 11);
            StyleConstants.setForeground(headerStyle, Color.GRAY);
            StyleConstants.setBold(headerStyle, true);
            
            String header = isSent ? "You  " + time : username + "  " + time;
            doc.insertString(doc.getLength(), header + "\n", headerStyle);
            
            // Message bubble
            Style msgStyle = chatPane.addStyle("Message", null);
            StyleConstants.setFontSize(msgStyle, 13);
            StyleConstants.setBackground(msgStyle, isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
            StyleConstants.setForeground(msgStyle, isSent ? Color.WHITE : Color.BLACK);
            StyleConstants.setLeftIndent(msgStyle, isSent ? 100 : 10);
            StyleConstants.setRightIndent(msgStyle, isSent ? 10 : 100);
            StyleConstants.setSpaceAbove(msgStyle, 5);
            StyleConstants.setSpaceBelow(msgStyle, 5);
            
            // Add padding inside bubble
            doc.insertString(doc.getLength(), "  " + message + "  \n", msgStyle);
            
            // Auto scroll to bottom
            chatPane.setCaretPosition(doc.getLength());
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void sendToSelected() {
        String target = clientList.getSelectedValue();
        if (target == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to chat with", 
                "No User Selected", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String msg = input.getText().trim();
        if (msg.isEmpty()) return;
        
        try {
            controlOut.writeUTF("PM|" + target + "|" + msg);
            controlOut.flush();
            appendMessage(target, msg, true);
            input.setText("");
            input.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to send message: " + e.getMessage(),
                "Send Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
