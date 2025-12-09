package smtp.mail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Email {
    private int id;
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sentDate;
    private boolean isRead;
    private List<String> attachments;

    public Email() {
        this.attachments = new ArrayList<>();
        this.isRead = false;
        this.sentDate = LocalDateTime.now();
    }

    public Email(String sender, String recipient, String subject, String body) {
        this();
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String attachment) {
        this.attachments.add(attachment);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", 
            isRead ? "✓" : "✉", 
            sender, 
            recipient, 
            subject);
    }
}
