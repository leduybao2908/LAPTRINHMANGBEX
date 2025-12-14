package smtp.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * Panel for video calling between users
 */
public class VideoCallPanel extends JPanel {
    private VideoCallClient videoClient;
    private JPanel localVideoPanel;
    private JPanel remoteVideoPanel;
    private JButton startCallButton;
    private JButton stopCallButton;
    private JTextField remoteHostField;
    private JTextField remotePortField;
    private JTextField localPortField;
    private JComboBox<String> webcamComboBox;
    private JLabel statusLabel;
    
    private BufferedImage localFrame;
    private BufferedImage remoteFrame;
    
    private Timer updateTimer;
    
    public VideoCallPanel(String username) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top panel with connection settings
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with video displays
        JPanel videoPanel = createVideoPanel();
        add(videoPanel, BorderLayout.CENTER);
        
        // Bottom panel with status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Update timer for repainting video frames
        updateTimer = new Timer(33, e -> {
            localVideoPanel.repaint();
            remoteVideoPanel.repaint();
        });
        updateTimer.start();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 152, 219));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("VIDEO CALL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Connection settings panel
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        settingsPanel.setOpaque(false);
        
        JLabel webcamLabel = new JLabel("Camera:");
        webcamLabel.setForeground(Color.WHITE);
        webcamComboBox = new JComboBox<>();
        loadWebcams();
        styleComboBox(webcamComboBox);
        
        JLabel localPortLabel = new JLabel("Local Port:");
        localPortLabel.setForeground(Color.WHITE);
        localPortField = new JTextField("5000");
        styleTextField(localPortField);
        
        JLabel remoteHostLabel = new JLabel("Remote Host:");
        remoteHostLabel.setForeground(Color.WHITE);
        remoteHostField = new JTextField("localhost");
        styleTextField(remoteHostField);
        
        JLabel remotePortLabel = new JLabel("Remote Port:");
        remotePortLabel.setForeground(Color.WHITE);
        remotePortField = new JTextField("5001");
        styleTextField(remotePortField);
        
        settingsPanel.add(webcamLabel);
        settingsPanel.add(webcamComboBox);
        settingsPanel.add(localPortLabel);
        settingsPanel.add(localPortField);
        settingsPanel.add(remoteHostLabel);
        settingsPanel.add(remoteHostField);
        settingsPanel.add(remotePortLabel);
        settingsPanel.add(remotePortField);
        
        panel.add(settingsPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        startCallButton = new JButton("START CALL");
        styleButton(startCallButton, new Color(46, 204, 113));
        startCallButton.addActionListener(this::startCall);
        
        stopCallButton = new JButton("STOP CALL");
        styleButton(stopCallButton, new Color(231, 76, 60));
        stopCallButton.setEnabled(false);
        stopCallButton.addActionListener(this::stopCall);
        
        buttonPanel.add(startCallButton);
        buttonPanel.add(stopCallButton);
        
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createVideoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Local video panel
        JPanel localContainer = new JPanel(new BorderLayout());
        localContainer.setBackground(Color.WHITE);
        localContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Your Video",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(52, 152, 219)
        ));
        
        localVideoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (localFrame != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(localFrame, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    String msg = "Camera Off";
                    FontMetrics fm = g.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(msg)) / 2;
                    int y = getHeight() / 2;
                    g.drawString(msg, x, y);
                }
            }
        };
        localVideoPanel.setPreferredSize(new Dimension(400, 300));
        localVideoPanel.setBackground(Color.BLACK);
        localContainer.add(localVideoPanel, BorderLayout.CENTER);
        
        // Remote video panel
        JPanel remoteContainer = new JPanel(new BorderLayout());
        remoteContainer.setBackground(Color.WHITE);
        remoteContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            "Remote Video",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(46, 204, 113)
        ));
        
        remoteVideoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (remoteFrame != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(remoteFrame, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    String msg = "Waiting for connection...";
                    FontMetrics fm = g.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(msg)) / 2;
                    int y = getHeight() / 2;
                    g.drawString(msg, x, y);
                }
            }
        };
        remoteVideoPanel.setPreferredSize(new Dimension(400, 300));
        remoteVideoPanel.setBackground(Color.BLACK);
        remoteContainer.add(remoteVideoPanel, BorderLayout.CENTER);
        
        panel.add(localContainer);
        panel.add(remoteContainer);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        
        statusLabel = new JLabel("Ready to call");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(52, 73, 94));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private void loadWebcams() {
        webcamComboBox.removeAllItems();
        java.util.List<com.github.sarxos.webcam.Webcam> webcams = VideoCallClient.getWebcams();
        
        if (webcams.isEmpty()) {
            webcamComboBox.addItem("No camera detected");
        } else {
            for (com.github.sarxos.webcam.Webcam webcam : webcams) {
                webcamComboBox.addItem(webcam.getName());
            }
        }
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(236, 240, 241));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(new Color(236, 240, 241));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(color.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(color);
                }
            }
        });
    }
    
    private void startCall(ActionEvent e) {
        // Prevent multiple clicks
        if (videoClient != null && videoClient.isActive()) {
            return;
        }
        
        try {
            int localPort = Integer.parseInt(localPortField.getText().trim());
            String remoteHost = remoteHostField.getText().trim();
            int remotePort = Integer.parseInt(remotePortField.getText().trim());
            
            if (!VideoCallClient.isWebcamAvailable()) {
                JOptionPane.showMessageDialog(this,
                    "No webcam detected. Please connect a webcam and try again.",
                    "Webcam Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            videoClient = new VideoCallClient(localPort, remoteHost, remotePort);
            
            // Set selected webcam
            int selectedIndex = webcamComboBox.getSelectedIndex();
            if (selectedIndex >= 0) {
                java.util.List<com.github.sarxos.webcam.Webcam> webcams = VideoCallClient.getWebcams();
                if (selectedIndex < webcams.size()) {
                    videoClient.setWebcam(webcams.get(selectedIndex));
                }
            }
            
            // Set callback for received frames
            videoClient.setOnFrameReceived(frame -> {
                remoteFrame = frame;
            });
            
            // Set callback for local frames
            videoClient.setOnLocalFrameCaptured(frame -> {
                localFrame = frame;
            });
            
            videoClient.startCall();
            
            startCallButton.setEnabled(false);
            stopCallButton.setEnabled(true);
            webcamComboBox.setEnabled(false);
            remoteHostField.setEnabled(false);
            remotePortField.setEnabled(false);
            localPortField.setEnabled(false);
            
            statusLabel.setText("Call in progress...");
            statusLabel.setForeground(new Color(46, 204, 113));
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid port number. Please enter valid numbers.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to start call: " + ex.getMessage(),
                "Call Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    

    
    private void stopCall(ActionEvent e) {
        if (videoClient != null) {
            videoClient.stopCall();
            videoClient = null;
        }
        
        localFrame = null;
        remoteFrame = null;
        
        startCallButton.setEnabled(true);
        stopCallButton.setEnabled(false);
        webcamComboBox.setEnabled(true);
        remoteHostField.setEnabled(true);
        remotePortField.setEnabled(true);
        localPortField.setEnabled(true);
        
        statusLabel.setText("Call ended");
        statusLabel.setForeground(new Color(231, 76, 60));
    }
}
