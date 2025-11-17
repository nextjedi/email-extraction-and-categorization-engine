package com.imp.classification.strategy;

import com.imp.shared.constant.MessageCategory;
import com.imp.shared.dto.ClassificationResult;
import com.imp.shared.dto.RawMessageDTO;
import com.imp.shared.strategy.ClassificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Rule-based classification strategy using pattern matching
 */
@Slf4j
@Component
public class RuleBasedClassificationStrategy implements ClassificationStrategy {

    // Transactional patterns
    private static final List<Pattern> TRANSACTIONAL_PATTERNS = List.of(
        Pattern.compile("\\$\\d+\\.\\d{2}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("invoice|receipt|payment|transaction|purchase|order", Pattern.CASE_INSENSITIVE),
        Pattern.compile("your order|order confirmation|payment received", Pattern.CASE_INSENSITIVE),
        Pattern.compile("bank|paypal|stripe|credit card", Pattern.CASE_INSENSITIVE)
    );

    // Job search patterns
    private static final List<Pattern> JOB_SEARCH_PATTERNS = List.of(
        Pattern.compile("job application|interview|position|career|hiring", Pattern.CASE_INSENSITIVE),
        Pattern.compile("linkedin|indeed|glassdoor", Pattern.CASE_INSENSITIVE),
        Pattern.compile("resume|cv|cover letter", Pattern.CASE_INSENSITIVE),
        Pattern.compile("job alert|new job|job opportunity", Pattern.CASE_INSENSITIVE)
    );

    // Subscription patterns
    private static final List<Pattern> SUBSCRIPTION_PATTERNS = List.of(
        Pattern.compile("newsletter|subscription|subscribe|unsubscribe", Pattern.CASE_INSENSITIVE),
        Pattern.compile("weekly digest|daily update|monthly summary", Pattern.CASE_INSENSITIVE),
        Pattern.compile("marketing|promotional", Pattern.CASE_INSENSITIVE)
    );

    // Travel patterns
    private static final List<Pattern> TRAVEL_PATTERNS = List.of(
        Pattern.compile("flight|hotel|booking|reservation|travel", Pattern.CASE_INSENSITIVE),
        Pattern.compile("check-in|boarding pass|itinerary", Pattern.CASE_INSENSITIVE),
        Pattern.compile("airbnb|uber|lyft|expedia|booking\\.com", Pattern.CASE_INSENSITIVE)
    );

    // Personal patterns
    private static final List<Pattern> PERSONAL_PATTERNS = List.of(
        Pattern.compile("@gmail\\.com|@yahoo\\.com|@outlook\\.com|@hotmail\\.com", Pattern.CASE_INSENSITIVE),
        Pattern.compile("re:|fwd:", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public ClassificationResult classify(RawMessageDTO message) {
        log.debug("Classifying message: {}", message.getMessageId());

        String content = buildSearchableContent(message);

        Map<MessageCategory, Double> scores = new HashMap<>();
        scores.put(MessageCategory.TRANSACTIONAL, calculateScore(content, TRANSACTIONAL_PATTERNS));
        scores.put(MessageCategory.JOB_SEARCH, calculateScore(content, JOB_SEARCH_PATTERNS));
        scores.put(MessageCategory.SUBSCRIPTION, calculateScore(content, SUBSCRIPTION_PATTERNS));
        scores.put(MessageCategory.TRAVEL, calculateScore(content, TRAVEL_PATTERNS));
        scores.put(MessageCategory.PERSONAL, calculateScore(content, PERSONAL_PATTERNS));

        // Find category with highest score
        MessageCategory primaryCategory = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(MessageCategory.OTHER);

        Double confidence = scores.get(primaryCategory);

        // If confidence too low, mark as OTHER
        if (confidence < 0.3) {
            primaryCategory = MessageCategory.OTHER;
        }

        return ClassificationResult.builder()
            .messageId(message.getMessageId())
            .userId(message.getUserId())
            .primaryCategory(primaryCategory)
            .categoryScores(scores)
            .classifierName(getStrategyName())
            .confidence(confidence)
            .extractedEntities(extractEntities(content))
            .classifiedAt(LocalDateTime.now())
            .correlationId(message.getCorrelationId())
            .build();
    }

    @Override
    public String getStrategyName() {
        return "rule-based";
    }

    @Override
    public int getPriority() {
        return 10; // High priority
    }

    private String buildSearchableContent(RawMessageDTO message) {
        StringBuilder sb = new StringBuilder();
        if (message.getSubject() != null) {
            sb.append(message.getSubject()).append(" ");
        }
        if (message.getBody() != null) {
            sb.append(message.getBody()).append(" ");
        }
        if (message.getSnippet() != null) {
            sb.append(message.getSnippet()).append(" ");
        }
        if (message.getFrom() != null) {
            sb.append(message.getFrom());
        }
        return sb.toString();
    }

    private double calculateScore(String content, List<Pattern> patterns) {
        int matches = 0;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(content).find()) {
                matches++;
            }
        }
        return (double) matches / patterns.size();
    }

    private List<String> extractEntities(String content) {
        List<String> entities = new ArrayList<>();

        // Extract amounts
        Pattern amountPattern = Pattern.compile("\\$\\d+\\.\\d{2}");
        var matcher = amountPattern.matcher(content);
        while (matcher.find()) {
            entities.add("amount:" + matcher.group());
        }

        // Extract dates (simplified)
        Pattern datePattern = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{2,4}");
        matcher = datePattern.matcher(content);
        while (matcher.find()) {
            entities.add("date:" + matcher.group());
        }

        return entities;
    }
}
