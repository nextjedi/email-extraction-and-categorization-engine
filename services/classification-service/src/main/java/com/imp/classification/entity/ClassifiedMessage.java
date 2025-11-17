package com.imp.classification.entity;

import com.imp.shared.constant.MessageCategory;
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
@Table(name = "classified_messages", indexes = {
    @Index(name = "idx_category", columnList = "primary_category"),
    @Index(name = "idx_user_category", columnList = "user_id,primary_category")
})
public class ClassifiedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageCategory primaryCategory;

    private Double confidence;

    @Column(nullable = false)
    private String classifierName;

    private LocalDateTime classifiedAt;

    private String correlationId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
