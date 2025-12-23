package smtp.mail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class MailSender extends JPanel {

    private String currentUser;
    private JTextField toField, subjectField;
    private JTextArea bodyArea;
    private List<File> attachments = new ArrayList<>();
    private JTable inboxTable, sentTable;
    private DefaultTableModel inboxModel, sentModel;
    private JLabel unreadLabel;
    private MailDatabase mailDB;

    public MailSender(String username) {
        this.currentUser = username;
        this.mailDB = MailDatabase.getInstance();
        
        setLayout(new BorderLayout());

        // Top panel với user info
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("📧 Internal Mail System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(title, BorderLayout.CENTER);
        
        unreadLabel = new JLabel("User: " + username);
        unreadLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topPanel.add(unreadLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tạo tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📨 Send Mail", createSendPanel());
        tabs.addTab("📥 Inbox", createInboxPanel());
        tabs.addTab("📤 Sent", createSentPanel());
        tabs.addTab("👥 Users", createUsersPanel());
        
        add(tabs, BorderLayout.CENTER);
        
        // Load dữ liệu ban đầu
        refreshInbox();
        refreshSent();
        updateUnreadCount();
    }

    private JPanel createSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        toField = new JTextField(30);
        subjectField = new JTextField(30);
        bodyArea = new JTextArea(15, 30);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);

        // To
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(toField, gbc);

        // Subject
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(subjectField, gbc);

        panel.add(form, BorderLayout.NORTH);

        // Body
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.add(new JLabel("Message:"), BorderLayout.NORTH);
        bodyPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        panel.add(bodyPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton attachBtn = new JButton("📎 Attach File");
        JButton sendBtn = new JButton("📧 Send Mail");
        JButton clearBtn = new JButton("🗑 Clear");

        attachBtn.addActionListener(e -> chooseAttachment());
        sendBtn.addActionListener(e -> sendMail());
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(attachBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(sendBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "From", "Subject", "Date", "Status", "Spam"};
        inboxModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inboxTable = new JTable(inboxModel);
        inboxTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        inboxTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        inboxTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        inboxTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        inboxTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        inboxTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        
        JScrollPane scrollPane = new JScrollPane(inboxTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton viewBtn = new JButton("👁 View");
        JButton refreshBtn = new JButton("🔄 Refresh");
        
        viewBtn.addActionListener(e -> viewSelectedEmail(inboxTable, true));
        refreshBtn.addActionListener(e -> {
            refreshInbox();
            updateUnreadCount();
        });
        
        buttonPanel.add(viewBtn);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createSentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "To", "Subject", "Date", "Spam"};
        sentModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sentTable = new JTable(sentModel);
        sentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        sentTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        sentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        sentTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        
        JScrollPane scrollPane = new JScrollPane(sentTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton viewBtn = new JButton("👁 View");
        JButton refreshBtn = new JButton("🔄 Refresh");
        
        viewBtn.addActionListener(e -> viewSelectedEmail(sentTable, false));
        refreshBtn.addActionListener(e -> refreshSent());
        
        buttonPanel.add(viewBtn);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(listModel);
        
        // Load users
        List<String> users = mailDB.getAllUsers();
        for (String user : users) {
            listModel.addElement(user);
        }
        
        JScrollPane scrollPane = new JScrollPane(userList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Available Users"));
        JLabel infoLabel = new JLabel("<html>Double-click a user to compose email<br>Default users: admin, user1, user2<br>Default password: admin123 or pass123</html>");
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        // Double click để compose
        userList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(currentUser)) {
                        toField.setText(selectedUser);
                        // Switch to send tab
                        ((JTabbedPane)panel.getParent()).setSelectedIndex(0);
                    }
                }
            }
        });
        
        return panel;
    }

    private void refreshInbox() {
        inboxModel.setRowCount(0);
        List<Email> emails = mailDB.getInbox(currentUser);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Email email : emails) {
            inboxModel.addRow(new Object[] {
                email.getId(),
                email.getSender(),
                email.getSubject(),
                email.getSentDate().format(formatter),
                email.isRead() ? "Read" : "Unread",
                email.isSpam() ? "⚠️ SPAM" : "✓"
            });
        }
    }

    private void refreshSent() {
        sentModel.setRowCount(0);
        List<Email> emails = mailDB.getSentEmails(currentUser);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Email email : emails) {
            sentModel.addRow(new Object[] {
                email.getId(),
                email.getRecipient(),
                email.getSubject(),
                email.getSentDate().format(formatter),
                email.isSpam() ? "⚠️ SPAM" : "✓"
            });
        }
    }

    private void viewSelectedEmail(JTable table, boolean isInbox) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int emailId = (int) table.getValueAt(selectedRow, 0);
            
            // Get email details from database
            List<Email> emails = isInbox ? mailDB.getInbox(currentUser) : mailDB.getSentEmails(currentUser);
            Email email = emails.stream()
                .filter(e -> e.getId() == emailId)
                .findFirst()
                .orElse(null);
            
            if (email != null) {
                if (isInbox && !email.isRead()) {
                    mailDB.markAsRead(emailId);
                    refreshInbox();
                    updateUnreadCount();
                }
                
                showEmailDialog(email);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an email to view");
        }
    }

    private void showEmailDialog(Email email) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Email Details", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(700, 500);
        
        JPanel detailsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        detailsPanel.add(new JLabel("From:"));
        detailsPanel.add(new JLabel(email.getSender()));
        detailsPanel.add(new JLabel("To:"));
        detailsPanel.add(new JLabel(email.getRecipient()));
        detailsPanel.add(new JLabel("Subject:"));
        detailsPanel.add(new JLabel(email.getSubject()));
        detailsPanel.add(new JLabel("Date:"));
        detailsPanel.add(new JLabel(email.getSentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        // Spam status with Naive Bayes score
        detailsPanel.add(new JLabel("Spam Status:"));
        JLabel spamLabel;
        if (email.isSpam()) {
            // Get spam score for display
            NaiveBayesSpamDetector nbDetector = NaiveBayesSpamDetector.getInstance();
            double spamScore = nbDetector.getSpamScore(email.getSubject(), email.getBody());
            spamLabel = new JLabel(String.format("⚠️ SPAM DETECTED (%.1f%% probability)", spamScore * 100));
            spamLabel.setForeground(Color.RED);
            spamLabel.setFont(spamLabel.getFont().deriveFont(Font.BOLD));
        } else {
            spamLabel = new JLabel("✓ Clean");
            spamLabel.setForeground(new Color(0, 150, 0));
        }
        detailsPanel.add(spamLabel);
        
        // Digital signature verification
        detailsPanel.add(new JLabel("Digital Signature:"));
        JLabel signatureLabel;
        if (email.getDigitalSignature() != null && !email.getDigitalSignature().isEmpty()) {
            try {
                String contentToVerify = email.getSubject() + email.getBody();
                boolean isValid = DigitalSignatureUtil.verify(contentToVerify, email.getDigitalSignature());
                if (isValid) {
                    signatureLabel = new JLabel("✓ Verified - Email is authentic");
                    signatureLabel.setForeground(new Color(0, 150, 0));
                    signatureLabel.setFont(signatureLabel.getFont().deriveFont(Font.BOLD));
                } else {
                    signatureLabel = new JLabel("✗ Invalid - Email may be tampered!");
                    signatureLabel.setForeground(Color.RED);
                    signatureLabel.setFont(signatureLabel.getFont().deriveFont(Font.BOLD));
                }
            } catch (Exception e) {
                signatureLabel = new JLabel("⚠️ Cannot verify: " + e.getMessage());
                signatureLabel.setForeground(Color.ORANGE);
            }
        } else {
            signatureLabel = new JLabel("⚠️ Not signed");
            signatureLabel.setForeground(Color.GRAY);
        }
        detailsPanel.add(signatureLabel);
        
        dialog.add(detailsPanel, BorderLayout.NORTH);
        
        JTextArea bodyArea = new JTextArea(email.getBody());
        bodyArea.setEditable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        dialog.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        
        if (!email.getAttachments().isEmpty()) {
            JPanel attachPanel = new JPanel();
            attachPanel.add(new JLabel("Attachments: " + email.getAttachments().size()));
            dialog.add(attachPanel, BorderLayout.SOUTH);
        }
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateUnreadCount() {
        int unread = mailDB.getUnreadCount(currentUser);
        unreadLabel.setText(String.format("User: %s | Unread: %d", currentUser, unread));
    }

    private void chooseAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            attachments.add(fileChooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Attachment added: " + fileChooser.getSelectedFile().getName());
        }
    }

    private void sendMail() {
        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter recipient");
            return;
        }

        // Check if recipient exists
        if (!mailDB.getAllUsers().contains(to)) {
            JOptionPane.showMessageDialog(this, "Recipient user does not exist!");
            return;
        }

        Email email = new Email(currentUser, to, subject, body);
        
        // Detect spam using Naive Bayes
        NaiveBayesSpamDetector nbDetector = NaiveBayesSpamDetector.getInstance();
        NaiveBayesSpamDetector.SpamClassification classification = nbDetector.classify(subject, body);
        boolean isSpam = classification.isSpam;
        email.setSpam(isSpam);
        
        if (isSpam) {
            String message = String.format(
                "⚠️ This email is classified as SPAM\n\n" +
                "Classification: %s\n" +
                "Spam Probability: %.1f%%\n\n" +
                "Do you still want to send it?",
                classification.confidence,
                classification.score * 100
            );
            
            int result = JOptionPane.showConfirmDialog(this, 
                message,
                "Spam Warning - Naive Bayes Classifier", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Generate digital signature
        try {
            String contentToSign = subject + body;
            String signature = DigitalSignatureUtil.sign(contentToSign);
            email.setDigitalSignature(signature);
        } catch (Exception e) {
            // If key files don't exist, generate them
            try {
                JOptionPane.showMessageDialog(this, 
                    "Generating digital signature keys...\nThis will only happen once.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
                KeyPairGeneratorUtil.generateAndSaveKeys();
                String contentToSign = subject + body;
                String signature = DigitalSignatureUtil.sign(contentToSign);
                email.setDigitalSignature(signature);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Warning: Could not generate digital signature: " + ex.getMessage(),
                    "Signature Error", JOptionPane.WARNING_MESSAGE);
            }
        }
        
        // Add attachments
        for (File file : attachments) {
            email.addAttachment(file.getAbsolutePath());
        }

        if (mailDB.sendEmail(email)) {
            String spamWarning = isSpam ? " (marked as spam)" : "";
            JOptionPane.showMessageDialog(this, "✓ Mail sent successfully!" + spamWarning);
            clearForm();
            refreshSent();
        } else {
            JOptionPane.showMessageDialog(this, "✗ Error sending mail");
        }
    }

    private void clearForm() {
        toField.setText("");
        subjectField.setText("");
        bodyArea.setText("");
        attachments.clear();
    }
}
