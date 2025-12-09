package smtp.mail;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class MailUtil {

    /**
     * Gửi email có hỗ trợ HTML, chữ ký số và cảnh báo spam.
     */
    public static void send(String host, int port, final String username, final String password,
                            String from, String to, String subject, String body, boolean useTls,
                            List<File> attachments) throws MessagingException {

        // --- Cấu hình SMTP ---
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", useTls ? "true" : "false");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        session.setDebug(false);

        // --- Soạn email ---
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject); // ✅ chỉ 1 tham số — đúng chuẩn Jakarta Mail

        // --- Digital Signature ---
        String signature = "";
        try {
            signature = DigitalSignatureUtil.sign(body);
        } catch (Exception e) {
            System.err.println("⚠️ Không thể ký nội dung: " + e.getMessage());
        }

        // --- Spam Detection ---
        boolean isSpam = SpamDetector.isSpam(subject, body);
        double score = SpamDetector.getSpamScore(subject, body);
        String spamWarning = isSpam
                ? String.format("<div style='background:#ffe6e6; color:#b30000; padding:10px; border-radius:5px; margin-bottom:15px;'>⚠️ This email might be spam (score: %.0f%%)</div>", score * 100)
                : "";

        // --- Nội dung HTML hoàn chỉnh ---
        String htmlBody = """
            <html>
              <body style="font-family:Segoe UI, sans-serif; background-color:#f4f6f8; margin:0; padding:20px;">
                <div style="max-width:600px; margin:auto; background:white; border-radius:10px;
                            box-shadow:0 2px 6px rgba(0,0,0,0.1); padding:30px;">
                  
                  <h2 style="color:#0078D7; text-align:center;">📧 Smart Mail Sender</h2>
                  
                  %s <!-- spam warning -->
                  
                  <div style="font-size:15px; color:#333; line-height:1.6; margin-top:15px;">
                    %s
                  </div>
                  
                  <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">
                  
                  <p style="font-size:13px; color:#999; text-align:center;">
                    Sent securely with digital signature.<br>
                    <span style="font-family:monospace; color:#666; word-break:break-all;">%s</span>
                  </p>
                </div>
              </body>
            </html>
            """.formatted(spamWarning, body.replace("\n", "<br>"), signature);

        // --- Body chính ---
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);

        // --- Thêm file đính kèm (nếu có) ---
        if (attachments != null) {
            for (File file : attachments) {
                if (file == null || !file.exists()) continue;
                try {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(file);
                    attachPart.setDataHandler(new DataHandler(fds));
                    attachPart.setFileName(file.getName());
                    multipart.addBodyPart(attachPart);
                } catch (Exception ex) {
                    System.err.println("⚠️ Lỗi khi đính kèm file " + file.getName() + ": " + ex.getMessage());
                }
            }
        }

        message.setContent(multipart);

        // --- Gửi mail ---
        Transport.send(message);
        System.out.println("✅ Email sent successfully (HTML + signature + spam warning)!");
    }

    // Tiện lợi khi không có file đính kèm
    public static void send(String host, int port, String username, String password,
                            String from, String to, String subject, String body, boolean useTls)
            throws MessagingException {
        send(host, port, username, password, from, to, subject, body, useTls, null);
    }
}
