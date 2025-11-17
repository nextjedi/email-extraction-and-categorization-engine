package com.imp.shared.dto;

import com.imp.shared.constant.MessageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of message classification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {

    private String messageId;
    private String userId;

    private MessageCategory primaryCategory;
    @Builder.Default
    private Map<MessageCategory, Double> categoryScores = new HashMap<>();

    private String classifierName; // rule-based, ml, hybrid
    private Double confidence;

    @Builder.Default
    private List<String> extractedEntities = List.of(); // Company names, dates, amounts, etc.

    private Map<String, String> metadata; // Category-specific metadata

    private LocalDateTime classifiedAt;
    private String correlationId;
}
