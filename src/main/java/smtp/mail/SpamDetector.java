package smtp.mail;

import java.util.*;

public class SpamDetector {

    // Danh sách từ khóa spam phổ biến
    private static final Set<String> SPAM_KEYWORDS = new HashSet<>(Arrays.asList(
        "win", "winner", "prize", "free", "money", "urgent", "offer", "bonus",
        "limited", "click here", "claim", "lottery", "guarantee", "credit",
        "loan", "cheap", "deal", "reward", "discount", "promotion"
    ));

    // Hàm kiểm tra spam
    public static boolean isSpam(String subject, String body) {
        if (subject == null && body == null) return false;

        String text = (subject + " " + body).toLowerCase();
        int count = 0;

        for (String keyword : SPAM_KEYWORDS) {
            if (text.contains(keyword)) {
                count++;
            }
        }

        return count >= 2; // nếu có >= 2 từ khóa spam thì đánh nhãn là spam
    }

    // Hàm trả về mức độ spam (cho UI hiển thị)
    public static double getSpamScore(String subject, String body) {
        String text = (subject + " " + body).toLowerCase();
        int count = 0;
        for (String keyword : SPAM_KEYWORDS) {
            if (text.contains(keyword)) count++;
        }
        return Math.min(1.0, count / 5.0); // tỉ lệ spam (0.0–1.0)
    }
}
