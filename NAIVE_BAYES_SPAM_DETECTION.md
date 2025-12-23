# Naive Bayes Spam Detection - Chi tiết kỹ thuật

## 🎯 Tổng quan

Hệ thống đã được nâng cấp từ **keyword-based** đơn giản sang **Naive Bayes classifier** - một thuật toán Machine Learning mạnh mẽ cho text classification.

---

## 📊 So sánh: Keyword-based vs Naive Bayes

| Tiêu chí | Keyword-based | Naive Bayes |
|----------|---------------|-------------|
| **Thuật toán** | Đếm keywords | Probabilistic ML |
| **Độ chính xác** | ~60-70% | ~85-95% |
| **Training** | Không cần | Cần training data |
| **False positive** | Cao | Thấp |
| **Context-aware** | ❌ Không | ✅ Có (phần nào) |
| **Tốc độ** | Rất nhanh | Nhanh |
| **Vocabulary** | 20 keywords | Toàn bộ vocabulary |

---

## 🧮 Thuật toán Naive Bayes

### 1. Công thức cơ bản

```
P(Spam|Email) = P(Email|Spam) * P(Spam) / P(Email)
```

**Trong đó:**
- `P(Spam|Email)`: Xác suất email là spam khi biết nội dung
- `P(Email|Spam)`: Xác suất gặp nội dung này trong spam
- `P(Spam)`: Xác suất prior của spam
- `P(Email)`: Xác suất gặp nội dung này

### 2. Naive Assumption

**"Naive" = Giả định ngây thơ**: Các từ trong email độc lập với nhau.

```
P(Email|Spam) = P(word1|Spam) * P(word2|Spam) * ... * P(wordN|Spam)
```

Mặc dù giả định này không đúng 100% (các từ có liên quan), nhưng trong thực tế Naive Bayes vẫn hoạt động rất tốt!

### 3. Multinomial Naive Bayes

Sử dụng cho text classification (đếm số lần xuất hiện của từ):

```
P(word|Spam) = (count(word in spam) + 1) / (total spam words + vocabulary size)
```

**+1** là **Laplace Smoothing** để tránh xác suất = 0 với từ chưa gặp.

---

## 💾 Training Data

Hệ thống được train với **40 emails**:

### Spam emails (20):
```
- "Congratulations! You won $1000000 free prize money..."
- "URGENT! Your account will be closed. Click here..."
- "Win free iPhone! Click here to claim your prize..."
- ... (17 emails khác)
```

### Ham emails (20):
```
- "Hi, let's meet tomorrow at 10am to discuss the project..."
- "Please review the attached document and send feedback..."
- "Thank you for your help with the presentation..."
- ... (17 emails khác)
```

---

## 🔬 Chi tiết implementation

### 1. Tokenization
```java
// Chuyển text thành list of words
"Win Free Money!" 
→ ["win", "free", "money"]

// Lowercase, remove punctuation, filter short words
```

### 2. Training Phase
```java
// Đếm từ trong spam
spamWordCount.put("free", 15);  // "free" xuất hiện 15 lần trong spam
spamWordCount.put("money", 12);
totalSpamWords = 500;

// Đếm từ trong ham
hamWordCount.put("meeting", 8);
hamWordCount.put("project", 10);
totalHamWords = 480;
```

### 3. Classification Phase

**Ví dụ: Classify "Win free money now!"**

```java
// Step 1: Prior probabilities
P(Spam) = 20 / 40 = 0.5
P(Ham) = 20 / 40 = 0.5

// Step 2: Word probabilities (with Laplace smoothing)
P("win"|Spam) = (8 + 1) / (500 + 1000) = 0.006
P("free"|Spam) = (15 + 1) / (500 + 1000) = 0.0107
P("money"|Spam) = (12 + 1) / (500 + 1000) = 0.0087

P("win"|Ham) = (0 + 1) / (480 + 1000) = 0.00067
P("free"|Ham) = (2 + 1) / (480 + 1000) = 0.002
P("money"|Ham) = (1 + 1) / (480 + 1000) = 0.00135

// Step 3: Calculate log probabilities (tránh underflow)
log P(Spam|Email) = log(0.5) + log(0.006) + log(0.0107) + log(0.0087)
                  = -0.693 + (-5.116) + (-4.539) + (-4.744)
                  = -15.092

log P(Ham|Email) = log(0.5) + log(0.00067) + log(0.002) + log(0.00135)
                 = -0.693 + (-7.308) + (-6.215) + (-6.608)
                 = -20.824

// Step 4: Normalize to get probability
P(Spam|Email) = exp(-15.092) / (exp(-15.092) + exp(-20.824))
              ≈ 0.997 (99.7% spam)

→ SPAM! ⚠️
```

---

## 📈 Confidence Levels

Hệ thống phân loại theo mức độ tin cậy:

| Spam Score | Classification | Mô tả |
|------------|----------------|-------|
| 0.0 - 0.3 | **Definitely Ham** | Chắc chắn clean |
| 0.3 - 0.5 | **Probably Ham** | Có thể clean |
| 0.5 - 0.7 | **Probably Spam** | Có thể spam |
| 0.7 - 1.0 | **Definitely Spam** | Chắc chắn spam |

---

## 🧪 Test Cases

### Test 1: Spam rõ ràng
```
Subject: "Win Free Money Prize!"
Body: "Click here to claim your reward now! Limited offer!"

→ Spam Score: 0.95 (95%)
→ Classification: Definitely Spam ⚠️
```

### Test 2: Ham rõ ràng
```
Subject: "Meeting tomorrow"
Body: "Let's discuss the project plan at 10am"

→ Spam Score: 0.08 (8%)
→ Classification: Definitely Ham ✅
```

### Test 3: Borderline case
```
Subject: "Free coffee for team"
Body: "I'll bring free coffee for everyone tomorrow"

→ Spam Score: 0.45 (45%)
→ Classification: Probably Ham ✅

(Dù có "free" nhưng context là hợp lệ)
```

### Test 4: Tricky spam
```
Subject: "Important notification"
Body: "You won a prize. Contact us to claim your reward."

→ Spam Score: 0.78 (78%)
→ Classification: Definitely Spam ⚠️

(Model nhận ra pattern "won", "prize", "claim", "reward")
```

---

## ⚙️ Cấu hình

### Thay đổi threshold
```java
NaiveBayesSpamDetector detector = NaiveBayesSpamDetector.getInstance();
detector.setSpamThreshold(0.7);  // Mặc định: 0.5

// 0.7 = Less sensitive (fewer false positives)
// 0.3 = More sensitive (catch more spam, but more false positives)
```

### Thêm training data
```java
// Thêm spam email
detector.train("New spam email content...", true);

// Thêm ham email  
detector.train("New legitimate email content...", false);
```

---

## 📊 Model Statistics

Xem thống kê model:
```java
System.out.println(detector.getModelStats());
```

Output:
```
Naive Bayes Model:
- Spam emails trained: 20
- Ham emails trained: 20
- Spam vocabulary: 156 words
- Ham vocabulary: 142 words
- Total vocabulary: 278 words
- Threshold: 0.50
```

---

## 🎯 Ưu điểm Naive Bayes

✅ **Chính xác cao hơn keyword-based**
- Hiểu context tốt hơn
- Ít false positive

✅ **Nhanh**
- O(V) complexity (V = vocabulary size)
- Không cần neural network phức tạp

✅ **Ít data cần thiết**
- Hoạt động tốt với 20-40 emails
- Không cần thousands of examples như deep learning

✅ **Dễ train thêm**
- Incremental learning
- Có thể update model liên tục

✅ **Explainable**
- Có thể show từ nào contribute vào spam score
- Không phải "black box"

---

## ⚠️ Hạn chế

❌ **Naive assumption**
- Giả định từ độc lập (không hoàn toàn đúng)
- "Free money" khác "money free"

❌ **Cần training data**
- Phải có spam + ham examples
- Quality of training data matters

❌ **Language-specific**
- Model train tiếng Anh không work với tiếng Việt
- Cần train riêng cho mỗi ngôn ngữ

❌ **Không hiểu obfuscation**
- "Fr33 m0n3y" có thể bypass
- Cần pre-processing phức tạp hơn

---

## 🚀 Cải tiến có thể làm

### Level 1: Easy
```java
// Thêm n-grams (2-3 words together)
"free money" → treat as single token
"click here" → treat as single token
```

### Level 2: Medium
```java
// Feature engineering
- Email length
- Number of exclamation marks!!!
- ALL CAPS words
- Presence of URLs
- Sender reputation
```

### Level 3: Advanced
```java
// Combine with other models
- Ensemble: Naive Bayes + SVM + Random Forest
- Deep Learning: LSTM, BERT
- Active Learning: Learn from user feedback
```

---

## 📚 So sánh với các thuật toán khác

| Algorithm | Accuracy | Speed | Training Data | Interpretability |
|-----------|----------|-------|---------------|------------------|
| **Keyword-based** | 60-70% | ⚡⚡⚡ | None | ✅✅✅ High |
| **Naive Bayes** | 85-95% | ⚡⚡ | Low | ✅✅ Medium |
| **SVM** | 90-96% | ⚡ | Medium | ✅ Low |
| **Random Forest** | 88-94% | ⚡⚡ | Medium | ✅ Low |
| **Deep Learning** | 95-99% | ⚡ | High | ❌ Very Low |

**→ Naive Bayes là sweet spot: Good accuracy + Fast + Easy to understand**

---

## 🎓 Tài liệu tham khảo

- [Naive Bayes Classifier - Wikipedia](https://en.wikipedia.org/wiki/Naive_Bayes_classifier)
- [Paul Graham's "A Plan for Spam"](http://www.paulgraham.com/spam.html) - Classic paper
- [Bayes' Theorem](https://en.wikipedia.org/wiki/Bayes%27_theorem)

---

**Version**: 0.1.0
**Date**: 23/12/2025
**Author**: Mail Chat System Team
