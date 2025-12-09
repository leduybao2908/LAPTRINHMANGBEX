package smtp;

import javax.swing.*;

import smtp.client.FilePanel;
import smtp.client.ChatPanel;
import smtp.mail.MailSender;
import smtp.server.FileServer;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Client main UI with bottom tabs. On startup asks for name and opens control connection.
 */
public class MainFrame extends JFrame {

    private final String clientName;
    private final DataOutputStream controlOut;
    private final DataInputStream controlIn;

    private final ChatPanel chatPanel;
    private final FilePanel filePanel;
    private final MailSender mailSender; // <= thêm vào

    public MainFrame(String serverHost, int controlPort) throws Exception {
        // ask name
        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Name", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) name = "Client" + (int)(Math.random()*1000);
        clientName = name;

        // connect control socket
        Socket controlSocket = new Socket(serverHost, controlPort);
        controlOut = new DataOutputStream(controlSocket.getOutputStream());
        controlIn = new DataInputStream(controlSocket.getInputStream());

        // send name
        controlOut.writeUTF(clientName);
        controlOut.flush();

        setTitle("Client - " + clientName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // ====== TABS ======
        JTabbedPane tabs = new JTabbedPane();

        // ---- MAIL TAB ----
        mailSender = new MailSender();   // TAKE REAL MAIL UI
        tabs.addTab("Mail", mailSender);

        // ---- FILE TAB ----
        filePanel = new FilePanel(clientName);
        tabs.addTab("File", filePanel);

        // ---- CHAT TAB ----
        chatPanel = new ChatPanel(clientName, controlOut);
        tabs.addTab("Chat", chatPanel);

        add(tabs, BorderLayout.CENTER);

        // listener
        new Thread(this::controlListener).start();
    }

    private void controlListener() {
        try {
            while (true) {
                String msg = controlIn.readUTF();
                if (msg.startsWith("CLIENTS|")) {
                    String listPart = msg.substring("CLIENTS|".length());
                    String[] names = listPart.split(",");
                    chatPanel.updateClientList(names);
                    filePanel.setClientList(names);
                } else if (msg.startsWith("PM|")) {
                    String[] p = msg.split("\\|", 3);
                    if (p.length >= 3) {
                        chatPanel.onPrivateMessage(p[1], p[2]);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lost connection to server: " + e.getMessage(),
                    "Disconnected",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    // entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mf = new MainFrame("localhost", FileServer.CONTROL_PORT);
                mf.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot connect to server: " + e.getMessage());
            }
        });
    }
}
