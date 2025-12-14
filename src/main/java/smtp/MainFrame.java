package smtp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import smtp.client.ChatPanel;
import smtp.client.FilePanel;
import smtp.client.VideoCallPanel;
import smtp.mail.MailSender;
import smtp.server.FileServer;

/**
 * Client main UI with bottom tabs. On startup asks for name and opens control connection.
 */
public class MainFrame extends JFrame {

    private String clientName;
    private String loggedInUser;
    private final DataOutputStream controlOut;
    private final DataInputStream controlIn;

    private final ChatPanel chatPanel;
    private final FilePanel filePanel;
    private final VideoCallPanel videoCallPanel;
    private MailSender mailSender;
    private JLabel statusLabel;

    public MainFrame(String serverHost, int controlPort, String username) throws Exception {
        this.loggedInUser = username;
        
        // ask name for chat/file
        String name = JOptionPane.showInputDialog(this, "Enter your display name for chat:", "Display Name", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) name = username;
        clientName = name;

        // connect control socket
        Socket controlSocket = new Socket(serverHost, controlPort);
        controlOut = new DataOutputStream(controlSocket.getOutputStream());
        controlIn = new DataInputStream(controlSocket.getInputStream());

        // send name
        controlOut.writeUTF(clientName);
        controlOut.flush();

        setTitle("Mail & Chat System - " + loggedInUser);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // Top panel with gradient background
        JPanel topPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(41, 128, 185), 
                    getWidth(), 0, new java.awt.Color(109, 213, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        statusLabel = new JLabel("  User: " + loggedInUser + " | Display: " + clientName);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(java.awt.Color.WHITE);
        topPanel.add(statusLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new java.awt.Color(231, 76, 60));
        logoutButton.setForeground(java.awt.Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 20, 8, 20));
        logoutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ====== TABS ======
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(java.awt.Color.WHITE);

        // ---- MAIL TAB ----
        mailSender = new MailSender(loggedInUser);
        tabs.addTab("  MAIL  ", mailSender);

        // ---- CHAT TAB ----
        chatPanel = new ChatPanel(clientName, controlOut);
        tabs.addTab("  CHAT  ", chatPanel);

        // ---- FILE TAB ----
        filePanel = new FilePanel(clientName);
        tabs.addTab("  FILE TRANSFER  ", filePanel);

        // ---- VIDEO CALL TAB ----
        videoCallPanel = new VideoCallPanel(loggedInUser);
        tabs.addTab("  VIDEO CALL  ", videoCallPanel);

        add(tabs, BorderLayout.CENTER);

        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        // listener
        new Thread(this::controlListener).start();
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            // Restart with login screen
            SwingUtilities.invokeLater(() -> {
                String username = LoginDialog.showLoginDialog(null);
                if (username != null) {
                    try {
                        MainFrame mf = new MainFrame("localhost", FileServer.CONTROL_PORT, username);
                        mf.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Cannot connect to server: " + ex.getMessage());
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            });
        }
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
        // Set FlatLaf Look and Feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Show login dialog first
            String username = LoginDialog.showLoginDialog(null);
            
            if (username != null) {
                try {
                    MainFrame mf = new MainFrame("localhost", FileServer.CONTROL_PORT, username);
                    mf.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Cannot connect to server: " + e.getMessage());
                    System.exit(0);
                }
            } else {
                // User cancelled login
                System.exit(0);
            }
        });
    }
}
