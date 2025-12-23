# Digital Signature và Spam Detection - Hướng dẫn sử dụng

## Tổng quan
Hệ thống đã được tích hợp 2 tính năng bảo mật mới:
1. **Digital Signature (Chữ ký số)**: Đảm bảo tính xác thực và toàn vẹn của email
2. **Spam Detection (Phát hiện thư rác)**: Tự động phát hiện và cảnh báo email spam

---

## 1. Digital Signature (Chữ ký số)

### Mô tả
- Mỗi email gửi đi sẽ tự động được ký bằng chữ ký số RSA 2048-bit
- Người nhận có thể xác minh tính xác thực và toàn vẹn của email
- Sử dụng thuật toán SHA256withRSA

### Cách hoạt động

#### Khi gửi email:
1. Hệ thống tự động tạo chữ ký từ nội dung email (subject + body)
2. Chữ ký được lưu cùng email trong database
3. Lần đầu tiên, hệ thống sẽ tự động tạo cặp khóa `private.key` và `public.key` trong thư mục gốc dự án

#### Khi xem email:
- Email hiển thị trạng thái xác thực:
  - ✅ **Verified - Email is authentic**: Email hợp lệ, chưa bị chỉnh sửa
  - ❌ **Invalid - Email may be tampered!**: Email có thể đã bị giả mạo
  - ⚠️ **Not signed**: Email chưa có chữ ký số (email cũ trước khi có tính năng)

### Files liên quan
- `DigitalSignatureUtil.java`: Xử lý ký và xác thực chữ ký
- `KeyPairGeneratorUtil.java`: Tạo cặp khóa RSA
- `private.key`: Khóa riêng (cần bảo mật!)
- `public.key`: Khóa công khai

### Lưu ý quan trọng
⚠️ **BẢO MẬT KHÓA RIÊNG**: 
- File `private.key` rất quan trọng và cần được bảo mật
- Không chia sẻ khóa riêng với người khác
- Nếu mất khóa, không thể ký email mới với chữ ký cũ

---

## 2. Spam Detection (Phát hiện thư rác)

### Mô tả
- Tự động phát hiện email spam dựa trên từ khóa đáng ngờ
- Cảnh báo người gửi và người nhận về email spam
- Đánh dấu email spam trong danh sách inbox

### Từ khóa spam được phát hiện
Hệ thống kiểm tra các từ khóa phổ biến trong email spam:
- win, winner, prize, free, money
- urgent, offer, bonus, limited
- click here, claim, lottery
- guarantee, credit, loan
- cheap, deal, reward, discount, promotion

### Cách hoạt động

#### Khi gửi email:
1. Hệ thống tự động quét subject và body
2. Nếu phát hiện ≥ 2 từ khóa spam → Cảnh báo
3. Người gửi có thể chọn:
   - **Yes**: Vẫn gửi (email được đánh dấu spam)
   - **No**: Hủy gửi và chỉnh sửa nội dung

#### Trong inbox:
- Cột "Spam" hiển thị:
  - ✓: Email sạch
  - ⚠️ SPAM: Email spam

#### Khi xem email:
- Hiển thị trạng thái spam:
  - ✅ **Clean**: Email sạch
  - ⚠️ **SPAM DETECTED**: Email spam (chữ đỏ, đậm)

### Tùy chỉnh
Để thay đổi danh sách từ khóa spam, chỉnh sửa file `SpamDetector.java`:
```java
private static final Set<String> SPAM_KEYWORDS = new HashSet<>(Arrays.asList(
    // Thêm hoặc xóa từ khóa tại đây
    "your_keyword"
));
```

---

## 3. Cách sử dụng

### Chạy ứng dụng
```bash
# Chạy server
mvn clean compile
java -cp target/classes smtp.server.FileServerUI

# Chạy client
java -cp target/classes smtp.MainFrame
# Hoặc
java -jar target/smtp-swing-0.1.0-all.jar
```

### Test tính năng

#### Test Digital Signature:
1. Gửi email bình thường
2. Mở email đã gửi → Xem trạng thái "Digital Signature"
3. Kết quả mong đợi: "✓ Verified - Email is authentic"

#### Test Spam Detection:
1. Gửi email với subject: "Win Free Money!"
2. Hệ thống sẽ cảnh báo: "⚠️ This email might be spam"
3. Sau khi gửi, email được đánh dấu "⚠️ SPAM" trong inbox

---

## 4. Kiến trúc kỹ thuật

### Database Schema (SQLite)
```sql
CREATE TABLE emails (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender TEXT NOT NULL,
    recipient TEXT NOT NULL,
    subject TEXT,
    body TEXT,
    sent_date TEXT NOT NULL,
    is_read INTEGER DEFAULT 0,
    digital_signature TEXT,      -- MỚI: Lưu chữ ký số
    is_spam INTEGER DEFAULT 0,   -- MỚI: Flag spam
    ...
);
```

### Classes chính
- `Email.java`: Model với 2 trường mới `digitalSignature` và `isSpam`
- `MailDatabase.java`: Lưu/đọc signature và spam flag
- `MailSender.java`: Tích hợp UI và logic
- `DigitalSignatureUtil.java`: RSA signature
- `SpamDetector.java`: Spam detection logic

---

## 5. Migration Database

Nếu đã có database cũ, hệ thống sẽ **TỰ ĐỘNG** thêm 2 columns mới:
- `digital_signature TEXT`
- `is_spam INTEGER DEFAULT 0`

Không cần làm gì thêm! Console sẽ hiển thị:
```
Added digital_signature column to emails table
Added is_spam column to emails table
```

---

## 6. Tính năng nâng cao

### Spam Score
Có thể sử dụng `SpamDetector.getSpamScore()` để có điểm spam (0.0 - 1.0):
```java
double score = SpamDetector.getSpamScore(subject, body);
// 0.0 = clean, 1.0 = definitely spam
```

### Custom Spam Threshold
Hiện tại: ≥ 2 từ khóa = spam
Có thể thay đổi trong `SpamDetector.isSpam()`:
```java
return count >= 3; // Đổi thành 3 thay vì 2
```

---

## 7. Troubleshooting

### "Cannot generate digital signature"
**Nguyên nhân**: Không thể tạo hoặc đọc key files

**Giải pháp**:
```bash
# Tạo lại keys thủ công
cd LAPTRINHMANGBEX
java -cp target/classes smtp.mail.KeyPairGeneratorUtil
```

### Email cũ không có signature
**Bình thường**: Email gửi trước khi có tính năng sẽ hiển thị "⚠️ Not signed"

### Spam detection quá nhạy/quá lỏng
**Giải pháp**: Chỉnh sửa `SPAM_KEYWORDS` và threshold trong `SpamDetector.java`

---

## 8. Demo Screenshots

### Gửi email với spam warning:
```
⚠️ This email might be spam (contains suspicious keywords).
Do you still want to send it?
[Yes] [No]
```

### Xem email với signature verified:
```
Digital Signature: ✓ Verified - Email is authentic
Spam Status: ✓ Clean
```

### Inbox với spam emails:
```
ID | From   | Subject          | Date       | Status | Spam
1  | user1  | Free Money!     | 2025-12-23 | Read   | ⚠️ SPAM
2  | admin  | Meeting Notes   | 2025-12-23 | Unread | ✓
```

---

## Kết luận

Hai tính năng này giúp:
- ✅ Đảm bảo tính xác thực của email (không bị giả mạo)
- ✅ Bảo vệ người dùng khỏi email spam/lừa đảo
- ✅ Tự động hóa hoàn toàn, không cần cấu hình thêm
- ✅ Tương thích với database cũ (auto migration)

**Tác giả**: Hệ thống Mail & Chat
**Phiên bản**: 0.1.0
**Ngày**: 23/12/2025
