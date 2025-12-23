package smtp.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Naive Bayes Spam Detector using Multinomial Naive Bayes algorithm
 * P(Spam|Email) = P(Email|Spam) * P(Spam) / P(Email)
 */
public class NaiveBayesSpamDetector {
    
    private Map<String, Integer> spamWordCount = new HashMap<>();
    private Map<String, Integer> hamWordCount = new HashMap<>();
    private int totalSpamWords = 0;
    private int totalHamWords = 0;
    private int spamEmailCount = 0;
    private int hamEmailCount = 0;
    private double spamThreshold = 0.5; // 50% probability threshold
    
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    
    // Singleton instance
    private static NaiveBayesSpamDetector instance;
    
    private NaiveBayesSpamDetector() {
        trainWithDefaultDataset();
    }
    
    public static synchronized NaiveBayesSpamDetector getInstance() {
        if (instance == null) {
            instance = new NaiveBayesSpamDetector();
        }
        return instance;
    }
    
    /**
     * Train with built-in dataset
     */
    private void trainWithDefaultDataset() {
        // Spam emails training data
        String[] spamEmails = {
            "Congratulations! You won $1000000 free prize money. Click here to claim now!",
            "URGENT! Your account will be closed. Click here immediately to verify your credit card.",
            "You are the winner of our lottery! Claim your free bonus now. Limited time offer!",
            "Make money fast! Work from home. Guaranteed income. Click here for free trial.",
            "Free pills! Cheap medication. Buy now! Limited stock. Discount 90%.",
            "Win free iPhone! Click here to claim your prize. Urgent offer ends today!",
            "You won the lottery! Click here to get your free money now!",
            "Lose weight fast! Free trial. Guaranteed results. Order now!",
            "Casino bonus! Play free games and win money. Click here now!",
            "Hot singles in your area! Click here to meet them now!",
            "Refinance your home loan! Low rates guaranteed. Click here for free quote.",
            "Get rich quick! Invest in cryptocurrency. Guaranteed profit. Limited offer!",
            "Your package is waiting! Click here to claim your free reward.",
            "Alert! Suspicious activity detected. Click here to secure your account urgently.",
            "Congratulations! You have been selected for a free vacation. Claim now!",
            "Make $5000 per week working from home! No experience needed. Click here!",
            "Free credit report! Click here to get your score now. Limited time!",
            "Win a free cruise! Enter now. Limited seats available. Click here!",
            "Debt relief! Consolidate your loans. Free consultation. Click here now!",
            "Hot deal! Buy cheap watches, bags, and shoes. Discount 80% off!"
        };
        
        // Ham (legitimate) emails training data
        String[] hamEmails = {
            "Hi, let's meet tomorrow at 10am to discuss the project plan.",
            "Please review the attached document and send me your feedback by Friday.",
            "The team meeting has been rescheduled to next Monday at 2pm.",
            "Thank you for your help with the presentation. It went very well.",
            "Can you send me the latest version of the report? I need it for the review.",
            "The conference call will start in 15 minutes. Here is the dial-in number.",
            "I will be out of office next week. Please contact Sarah for urgent matters.",
            "Great job on the demo! The client was very impressed with your work.",
            "The project deadline has been extended to next month. Please update your schedule.",
            "Reminder: Submit your timesheet by end of day today.",
            "Happy birthday! Hope you have a wonderful day with your family.",
            "The system maintenance is scheduled for Saturday night. Please save your work.",
            "Thanks for attending the meeting. Here are the minutes and action items.",
            "Please join us for lunch on Friday to celebrate the successful launch.",
            "The training session will be held in conference room A at 9am tomorrow.",
            "I have reviewed your proposal. Let's discuss the details next week.",
            "The quarterly results look great! Congratulations to the entire team.",
            "Please RSVP for the company event by next Tuesday. Looking forward to seeing you.",
            "Your presentation slides look good. Just minor formatting changes needed.",
            "The parking lot will be closed for repairs this weekend. Please use the street parking."
        };
        
        // Train with spam emails
        for (String email : spamEmails) {
            train(email, true);
        }
        
        // Train with ham emails
        for (String email : hamEmails) {
            train(email, false);
        }
        
        System.out.println("✓ Naive Bayes model trained with " + spamEmailCount + " spam and " + hamEmailCount + " ham emails");
    }
    
    /**
     * Train the model with an email
     */
    public void train(String text, boolean isSpam) {
        List<String> words = tokenize(text);
        
        if (isSpam) {
            spamEmailCount++;
            for (String word : words) {
                spamWordCount.put(word, spamWordCount.getOrDefault(word, 0) + 1);
                totalSpamWords++;
            }
        } else {
            hamEmailCount++;
            for (String word : words) {
                hamWordCount.put(word, hamWordCount.getOrDefault(word, 0) + 1);
                totalHamWords++;
            }
        }
    }
    
    /**
     * Classify email as spam or not
     */
    public boolean isSpam(String subject, String body) {
        if (subject == null && body == null) return false;
        
        String text = (subject != null ? subject : "") + " " + (body != null ? body : "");
        double spamProbability = calculateSpamProbability(text);
        
        return spamProbability > spamThreshold;
    }
    
    /**
     * Get spam probability score (0.0 to 1.0)
     */
    public double getSpamScore(String subject, String body) {
        if (subject == null && body == null) return 0.0;
        
        String text = (subject != null ? subject : "") + " " + (body != null ? body : "");
        return calculateSpamProbability(text);
    }
    
    /**
     * Calculate spam probability using Naive Bayes
     * P(Spam|Email) = P(Email|Spam) * P(Spam) / P(Email)
     */
    private double calculateSpamProbability(String text) {
        List<String> words = tokenize(text);
        
        if (words.isEmpty()) return 0.0;
        
        // Prior probabilities
        double priorSpam = (double) spamEmailCount / (spamEmailCount + hamEmailCount);
        double priorHam = (double) hamEmailCount / (spamEmailCount + hamEmailCount);
        
        // Calculate log probabilities to avoid underflow
        double logSpamProb = Math.log(priorSpam);
        double logHamProb = Math.log(priorHam);
        
        // Vocabulary size for Laplace smoothing
        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(spamWordCount.keySet());
        vocabulary.addAll(hamWordCount.keySet());
        int vocabSize = vocabulary.size();
        
        // Calculate likelihood for each word
        for (String word : words) {
            // Laplace smoothing: P(word|class) = (count + 1) / (total + vocabSize)
            double spamWordProb = (double) (spamWordCount.getOrDefault(word, 0) + 1) / (totalSpamWords + vocabSize);
            double hamWordProb = (double) (hamWordCount.getOrDefault(word, 0) + 1) / (totalHamWords + vocabSize);
            
            logSpamProb += Math.log(spamWordProb);
            logHamProb += Math.log(hamWordProb);
        }
        
        // Convert back from log space and normalize
        // Using log-sum-exp trick for numerical stability
        double maxLog = Math.max(logSpamProb, logHamProb);
        double spamExp = Math.exp(logSpamProb - maxLog);
        double hamExp = Math.exp(logHamProb - maxLog);
        
        return spamExp / (spamExp + hamExp);
    }
    
    /**
     * Tokenize text into words
     */
    private List<String> tokenize(String text) {
        if (text == null) return new ArrayList<>();
        
        List<String> words = new ArrayList<>();
        String[] tokens = text.toLowerCase().split("\\s+");
        
        for (String token : tokens) {
            // Remove punctuation and keep only alphanumeric
            String word = token.replaceAll("[^a-z0-9]", "");
            if (!word.isEmpty() && word.length() > 2) { // Ignore very short words
                words.add(word);
            }
        }
        
        return words;
    }
    
    /**
     * Get detailed classification info
     */
    public SpamClassification classify(String subject, String body) {
        if (subject == null && body == null) {
            return new SpamClassification(false, 0.0, "Empty email");
        }
        
        String text = (subject != null ? subject : "") + " " + (body != null ? body : "");
        double spamScore = calculateSpamProbability(text);
        boolean isSpam = spamScore > spamThreshold;
        
        String confidence;
        if (spamScore < 0.3) {
            confidence = "Definitely Ham (Clean)";
        } else if (spamScore < 0.5) {
            confidence = "Probably Ham";
        } else if (spamScore < 0.7) {
            confidence = "Probably Spam";
        } else {
            confidence = "Definitely Spam";
        }
        
        return new SpamClassification(isSpam, spamScore, confidence);
    }
    
    /**
     * Classification result
     */
    public static class SpamClassification {
        public final boolean isSpam;
        public final double score;
        public final String confidence;
        
        public SpamClassification(boolean isSpam, double score, String confidence) {
            this.isSpam = isSpam;
            this.score = score;
            this.confidence = confidence;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%.1f%% spam probability)", confidence, score * 100);
        }
    }
    
    /**
     * Get model statistics
     */
    public String getModelStats() {
        return String.format(
            "Naive Bayes Model:\n" +
            "- Spam emails trained: %d\n" +
            "- Ham emails trained: %d\n" +
            "- Spam vocabulary: %d words\n" +
            "- Ham vocabulary: %d words\n" +
            "- Total vocabulary: %d words\n" +
            "- Threshold: %.2f",
            spamEmailCount, hamEmailCount,
            spamWordCount.size(), hamWordCount.size(),
            new HashSet<String>() {{ addAll(spamWordCount.keySet()); addAll(hamWordCount.keySet()); }}.size(),
            spamThreshold
        );
    }
    
    /**
     * Set spam threshold (default 0.5)
     */
    public void setSpamThreshold(double threshold) {
        if (threshold >= 0.0 && threshold <= 1.0) {
            this.spamThreshold = threshold;
        }
    }
}
