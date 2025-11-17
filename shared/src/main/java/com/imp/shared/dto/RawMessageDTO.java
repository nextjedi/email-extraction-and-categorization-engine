package com.imp.shared.dto;

import com.imp.shared.constant.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Raw message extracted and ready for classification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMessageDTO {

    private String messageId;
    private String sourceId;
    private SourceType sourceType;
    private String userId;

    private String subject;
    private String body;
    private String snippet;

    private String from;
    @Builder.Default
    private List<String> to = new ArrayList<>();

    private LocalDateTime receivedAt;
    private LocalDateTime extractedAt;

    @Builder.Default
    private List<String> attachmentFilenames = new ArrayList<>();

    private String threadId;
    private String conversationId;

    @Builder.Default
    private List<String> labels = new ArrayList<>();

    // Kafka metadata
    private String correlationId;
    private Integer retryCount;
}
