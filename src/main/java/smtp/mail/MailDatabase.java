package smtp.mail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MailDatabase {
    private static final String DB_PATH = "mail_data/emails.db";
    private static final String ATTACHMENTS_DIR = "mail_data/attachments/";
    private static MailDatabase instance;
    private Connection connection;

    private MailDatabase() {
        try {
            // Tạo thư mục nếu chưa tồn tại
            File dbDir = new File("mail_data");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            File attachDir = new File(ATTACHMENTS_DIR);
            if (!attachDir.exists()) {
                attachDir.mkdirs();
            }

            // Kết nối database
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            initDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized MailDatabase getInstance() {
        if (instance == null) {
            instance = new MailDatabase();
        }
        return instance;
    }

    private void initDatabase() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                full_name TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createEmailsTable = """
            CREATE TABLE IF NOT EXISTS emails (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT NOT NULL,
                recipient TEXT NOT NULL,
                subject TEXT,
                body TEXT,
                sent_date TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                digital_signature TEXT,
                is_spam INTEGER DEFAULT 0,
                FOREIGN KEY (sender) REFERENCES users(username),
                FOREIGN KEY (recipient) REFERENCES users(username)
            )
        """;

        String createAttachmentsTable = """
            CREATE TABLE IF NOT EXISTS attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email_id INTEGER NOT NULL,
                filename TEXT NOT NULL,
                filepath TEXT NOT NULL,
                FOREIGN KEY (email_id) REFERENCES emails(id)
            )
        """;

        Statement stmt = connection.createStatement();
        stmt.execute(createUsersTable);
        stmt.execute(createEmailsTable);
        stmt.execute(createAttachmentsTable);
        stmt.close();

        // Migrate existing database schema
        migrateDatabase();

        // Thêm user mặc định nếu chưa có
        createDefaultUsers();
    }

    private void migrateDatabase() {
        try {
            Statement stmt = connection.createStatement();
            
            // Check if digital_signature column exists
            try {
                ResultSet rs = stmt.executeQuery("SELECT digital_signature FROM emails LIMIT 1");
                rs.close();
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE emails ADD COLUMN digital_signature TEXT");
                System.out.println("Added digital_signature column to emails table");
            }
            
            // Check if is_spam column exists
            try {
                ResultSet rs = stmt.executeQuery("SELECT is_spam FROM emails LIMIT 1");
                rs.close();
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE emails ADD COLUMN is_spam INTEGER DEFAULT 0");
                System.out.println("Added is_spam column to emails table");
            }
            
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultUsers() {
        try {
            String checkUser = "SELECT COUNT(*) FROM users";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(checkUser);
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Thêm một số user mặc định
                createUser("admin", "admin123", "Administrator");
                createUser("user1", "pass123", "User One");
                createUser("user2", "pass123", "User Two");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createUser(String username, String password, String fullName) {
        String sql = "INSERT INTO users (username, password, full_name) VALUES (?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Trong thực tế nên hash password
            pstmt.setString(3, fullName);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                rs.close();
                pstmt.close();
                return storedPassword.equals(password);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean sendEmail(Email email) {
        String sql = "INSERT INTO emails (sender, recipient, subject, body, sent_date, digital_signature, is_spam) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email.getSender());
            pstmt.setString(2, email.getRecipient());
            pstmt.setString(3, email.getSubject());
            pstmt.setString(4, email.getBody());
            pstmt.setString(5, email.getSentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(6, email.getDigitalSignature());
            pstmt.setInt(7, email.isSpam() ? 1 : 0);
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (affectedRows > 0) {
                // Get the last inserted ID using SQLite's last_insert_rowid()
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    int emailId = rs.getInt(1);
                    email.setId(emailId);
                    
                    // Lưu attachments nếu có
                    for (String attachment : email.getAttachments()) {
                        saveAttachment(emailId, attachment);
                    }
                }
                rs.close();
                stmt.close();
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveAttachment(int emailId, String sourcePath) {
        try {
            Path source = Paths.get(sourcePath);
            String filename = source.getFileName().toString();
            String destPath = ATTACHMENTS_DIR + emailId + "_" + filename;
            
            // Copy file
            Files.copy(source, Paths.get(destPath));
            
            // Lưu vào database
            String sql = "INSERT INTO attachments (email_id, filename, filepath) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, emailId);
            pstmt.setString(2, filename);
            pstmt.setString(3, destPath);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Email> getInbox(String username) {
        List<Email> emails = new ArrayList<>();
        String sql = "SELECT * FROM emails WHERE recipient = ? ORDER BY sent_date DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Email email = new Email();
                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setRecipient(rs.getString("recipient"));
                email.setSubject(rs.getString("subject"));
                email.setBody(rs.getString("body"));
                email.setSentDate(LocalDateTime.parse(rs.getString("sent_date"), 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                email.setRead(rs.getInt("is_read") == 1);
                email.setDigitalSignature(rs.getString("digital_signature"));
                email.setSpam(rs.getInt("is_spam") == 1);
                
                // Load attachments
                email.setAttachments(getAttachments(email.getId()));
                
                emails.add(email);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return emails;
    }

    public List<Email> getSentEmails(String username) {
        List<Email> emails = new ArrayList<>();
        String sql = "SELECT * FROM emails WHERE sender = ? ORDER BY sent_date DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Email email = new Email();
                email.setId(rs.getInt("id"));
                email.setSender(rs.getString("sender"));
                email.setRecipient(rs.getString("recipient"));
                email.setSubject(rs.getString("subject"));
                email.setBody(rs.getString("body"));
                email.setSentDate(LocalDateTime.parse(rs.getString("sent_date"), 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                email.setRead(rs.getInt("is_read") == 1);
                email.setDigitalSignature(rs.getString("digital_signature"));
                email.setSpam(rs.getInt("is_spam") == 1);
                
                email.setAttachments(getAttachments(email.getId()));
                
                emails.add(email);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return emails;
    }

    private List<String> getAttachments(int emailId) {
        List<String> attachments = new ArrayList<>();
        String sql = "SELECT filepath FROM attachments WHERE email_id = ?";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, emailId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attachments.add(rs.getString("filepath"));
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return attachments;
    }

    public void markAsRead(int emailId) {
        String sql = "UPDATE emails SET is_read = 1 WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, emailId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadCount(String username) {
        String sql = "SELECT COUNT(*) FROM emails WHERE recipient = ? AND is_read = 0";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                pstmt.close();
                return count;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
