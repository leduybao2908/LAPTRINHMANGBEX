package smtp.client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.util.Arrays;

/**
 * ChatPanel uses the control DataOutputStream (passed from MainFrame) to send PM commands.
 * It receives messages via the MainFrame control listener and calls onPrivateMessage / updateClientList.
 */
public class ChatPanel extends JPanel {

    private final String clientName;
    private final DataOutputStream controlOut;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> clientList = new JList<>(model);
    private final JTextArea chatArea = new JTextArea();
    private final JTextField input = new JTextField();

    public ChatPanel(String clientName, DataOutputStream controlOut) {
        this.clientName = clientName;
        this.controlOut = controlOut;

        setLayout(new BorderLayout());
        clientList.setBorder(BorderFactory.createTitledBorder("Online clients"));
        add(new JScrollPane(clientList), BorderLayout.WEST);

        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(input, BorderLayout.CENTER);
        JButton send = new JButton("Send");
        bottom.add(send, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        clientList.setPreferredSize(new Dimension(150, 0));

        send.addActionListener(e -> sendToSelected());
        input.addActionListener(e -> sendToSelected());
    }

    public void updateClientList(String[] clients) {
        SwingUtilities.invokeLater(() -> {
            model.clear();
            Arrays.stream(clients).filter(s -> s != null && !s.isEmpty() && !s.equals(clientName)).forEach(model::addElement);
        });
    }

    public void onPrivateMessage(String from, String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(from + " -> you: " + message + "\n"));
    }

    private void sendToSelected() {
        String target = clientList.getSelectedValue();
        if (target == null) { JOptionPane.showMessageDialog(this, "Select a client to message"); return; }
        String msg = input.getText().trim();
        if (msg.isEmpty()) return;
        try {
            controlOut.writeUTF("PM|" + target + "|" + msg);
            controlOut.flush();
            chatArea.append("You -> " + target + ": " + msg + "\n");
            input.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Send failed: " + e.getMessage());
        }
    }
}
