# Quick Start Guide - Mail Chat System với Digital Signature & Spam Detection

## 🚀 Cách chạy ứng dụng

### Bước 1: Build dự án (chỉ cần làm 1 lần hoặc khi có thay đổi code)
```bash
mvn clean package
```

### Bước 2: Chạy Server
**Cách 1: Double-click file**
```
run-server.bat
```

**Cách 2: Command line**
```bash
java -cp target\smtp-swing-0.1.0-all.jar smtp.ServerStarter
```

### Bước 3: Chạy Client (sau khi server đã chạy)
**Cách 1: Double-click file**
```
run-client.bat
```

**Cách 2: Command line**
```bash
java -jar target\smtp-swing-0.1.0-all.jar
```

---

## 🧪 Test Digital Signature & Spam Detection

### Test 1: Gửi email SPAM
1. Đăng nhập với `admin` / `admin123`
2. Tab MAIL → Send Mail
3. Gửi email:
   - **To**: user1
   - **Subject**: `Win Free Money Prize!`
   - **Body**: `Click here to claim your bonus now! Limited offer!`
4. ✅ **Kết quả**: Sẽ có cảnh báo spam, inbox hiển thị "⚠️ SPAM"

### Test 2: Gửi email CLEAN với Digital Signature
1. Gửi email bình thường:
   - **To**: user1
   - **Subject**: `Meeting tomorrow`
   - **Body**: `Let's meet at 10am`
2. ✅ **Kết quả**: Email được ký tự động, không có cảnh báo spam

### Test 3: Xem trạng thái Digital Signature
1. Đăng nhập với `user1` / `pass123`
2. Tab MAIL → Inbox
3. Click View email
4. ✅ **Kết quả**: 
   - Email mới: "✓ Verified - Email is authentic"
   - Email cũ (trước khi có tính năng): "⚠️ Not signed"

---

## ⚠️ Lưu ý quan trọng

### Email "Not signed" là BÌNH THƯỜNG nếu:
- Email đã tồn tại TRƯỚC khi bạn cập nhật code
- Chỉ email gửi MỚI (sau khi cập nhật) mới có chữ ký

### Để test đúng:
1. ✅ **XÓA database cũ** (nếu muốn test từ đầu):
   ```bash
   rd /s /q mail_data
   ```
2. ✅ **Gửi email MỚI** sau khi cập nhật code
3. ✅ Email mới sẽ có đầy đủ Digital Signature và Spam Detection

---

## 📋 Accounts mặc định

| Username | Password  | Role          |
|----------|-----------|---------------|
| admin    | admin123  | Administrator |
| user1    | pass123   | User          |
| user2    | pass123   | User          |

---

## 🔧 Troubleshooting

### "Connection refused"
**Nguyên nhân**: Server chưa chạy hoặc đã tắt

**Giải pháp**: Chạy `run-server.bat` trước, sau đó mới chạy client

### "Not signed" trên TẤT CẢ email
**Nguyên nhân**: Files `private.key` / `public.key` bị lỗi

**Giải pháp**:
```bash
# Xóa keys cũ
del private.key
del public.key

# Keys mới sẽ tự động tạo khi gửi email lần đầu
```

### Server không chạy
**Giải pháp**: Đảm bảo đã build:
```bash
mvn clean package
```

---

## 📁 Files quan trọng

| File | Mục đích |
|------|----------|
| `private.key` | Khóa riêng để ký email (BẢO MẬT!) |
| `public.key` | Khóa công khai để xác thực |
| `mail_data/emails.db` | Database lưu emails |
| `run-server.bat` | Script chạy server |
| `run-client.bat` | Script chạy client |

---

## ✨ Tính năng

✅ Digital Signature với RSA 2048-bit
✅ Spam Detection với 20+ keywords
✅ Auto database migration
✅ Chat realtime
✅ File transfer
✅ Video call

**Version**: 0.1.0
**Date**: 23/12/2025
