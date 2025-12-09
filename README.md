# Simple SMTP Mail Sender (Swing)

This is a minimal Java Swing application that lets you send an email via SMTP.

Requirements
- Java 11+
- Maven

Build

```powershell
mvn -DskipTests package
```

Run

```powershell
java -jar target/smtp-swing-0.1.0.jar
```

Notes
- For Gmail, you must create an App Password and enable SMTP for the account, or use another SMTP provider.
- Default host is smtp.gmail.com and port 587 (TLS)."