package com.imp.extraction.entity;

import com.imp.shared.constant.SourceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "extracted_messages", indexes = {
    @Index(name = "idx_user_source", columnList = "user_id,source_type"),
    @Index(name = "idx_source_id", columnList = "source_id"),
    @Index(name = "idx_extracted_at", columnList = "extracted_at")
})
public class ExtractedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Column(nullable = false)
    private String userId;

    @Column(length = 1000)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(length = 500)
    private String snippet;

    @Column(length = 500)
    private String fromAddress;

    @Column(columnDefinition = "TEXT")
    private String toAddresses;

    private LocalDateTime receivedAt;
    private LocalDateTime extractedAt;

    private String threadId;
    private String conversationId;

    @Column(columnDefinition = "TEXT")
    private String labels;

    private Boolean isRead;
    private Boolean isStarred;

    @Column(nullable = false)
    private Boolean publishedToKafka = false;

    private String correlationId;

    @PrePersist
    protected void onCreate() {
        extractedAt = LocalDateTime.now();
    }
}
