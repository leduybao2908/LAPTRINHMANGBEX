package smtp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import smtp.mail.MailDatabase;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private String authenticatedUser = null;
    private MailDatabase mailDB;

    public LoginDialog(Frame parent) {
        super(parent, "Login - Mail System", true);
        mailDB = MailDatabase.getInstance();
        
        setLayout(new BorderLayout(0, 0));
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(false);
        
        // Main container with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), 0, getHeight(), new Color(109, 213, 250));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title Panel with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Mail System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Form Panel with white background
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(welcomeLabel, gbc);

        // Reset insets
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridwidth = 1;

        // Username label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        formPanel.add(usernameField, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        formPanel.add(passwordField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(140, 40));
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        registerButton = new JButton("REGISTER");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(140, 40));
        registerButton.setBackground(new Color(52, 152, 219));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("<html><center><i>Default: admin/admin123, user1/pass123, user2/pass123</i></center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(127, 140, 141));
        infoPanel.add(infoLabel);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(infoPanel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Event handlers
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> showRegisterDialog());
        
        // Enter key support
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mailDB.authenticateUser(username, password)) {
            authenticatedUser = username;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void showRegisterDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField newUsername = new JTextField(15);
        JPasswordField newPassword = new JPasswordField(15);
        JTextField fullName = new JTextField(15);

        panel.add(new JLabel("Username:"));
        panel.add(newUsername);
        panel.add(new JLabel("Password:"));
        panel.add(newPassword);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullName);

        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Register New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = newUsername.getText().trim();
            String password = new String(newPassword.getPassword());
            String name = fullName.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Username and password are required", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (mailDB.createUser(username, password, name.isEmpty() ? username : name)) {
                JOptionPane.showMessageDialog(this, 
                    "Registration successful! You can now login.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(username);
                passwordField.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Registration failed. Username may already exist.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static String showLoginDialog(Frame parent) {
        LoginDialog dialog = new LoginDialog(parent);
        dialog.setVisible(true);
        return dialog.getAuthenticatedUser();
    }
}
