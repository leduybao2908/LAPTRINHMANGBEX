package smtp.mail;

import smtp.mail.MailUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailSender extends JPanel {

    private JTextField hostField, portField, userField, passField;
    private JTextField fromField, toField, subjectField;
    private JTextArea bodyArea;
    private JCheckBox tlsCheck;
    private List<File> attachments = new ArrayList<>();

    public MailSender() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("📧 Mail Sender", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(10, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        hostField = new JTextField("smtp.gmail.com");
        portField = new JTextField("587");
        userField = new JTextField();
        passField = new JTextField();
        fromField = new JTextField();
        toField = new JTextField();
        subjectField = new JTextField();
        bodyArea = new JTextArea(10, 30);

        tlsCheck = new JCheckBox("Use TLS", true);

        form.add(new JLabel("SMTP Host:"));
        form.add(hostField);

        form.add(new JLabel("SMTP Port:"));
        form.add(portField);

        form.add(new JLabel("Username:"));
        form.add(userField);

        form.add(new JLabel("Password:"));
        form.add(passField);

        form.add(new JLabel("From:"));
        form.add(fromField);

        form.add(new JLabel("To:"));
        form.add(toField);

        form.add(new JLabel("Subject:"));
        form.add(subjectField);

        form.add(new JLabel("Use TLS:"));
        form.add(tlsCheck);

        add(form, BorderLayout.WEST);

        JScrollPane scroll = new JScrollPane(bodyArea);
        add(scroll, BorderLayout.CENTER);

        JButton attachBtn = new JButton("Add Attachment");
        JButton sendBtn = new JButton("Send Mail");

        JPanel bottom = new JPanel();
        bottom.add(attachBtn);
        bottom.add(sendBtn);
        add(bottom, BorderLayout.SOUTH);

        attachBtn.addActionListener(e -> chooseAttachment());
        sendBtn.addActionListener(e -> sendMail());
    }

    private void chooseAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            attachments.add(fileChooser.getSelectedFile());
        }
    }

    private void sendMail() {
        try {
            MailUtil.send(
                    hostField.getText(),
                    Integer.parseInt(portField.getText()),
                    userField.getText(),
                    passField.getText(),
                    fromField.getText(),
                    toField.getText(),
                    subjectField.getText(),
                    bodyArea.getText(),
                    tlsCheck.isSelected(),
                    attachments
            );

            JOptionPane.showMessageDialog(this, "Mail sent!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
