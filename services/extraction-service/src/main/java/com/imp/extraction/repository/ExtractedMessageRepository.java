package com.imp.extraction.repository;

import com.imp.extraction.entity.ExtractedMessage;
import com.imp.shared.constant.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExtractedMessageRepository extends JpaRepository<ExtractedMessage, Long> {

    Optional<ExtractedMessage> findBySourceIdAndSourceType(String sourceId, SourceType sourceType);

    boolean existsBySourceIdAndSourceType(String sourceId, SourceType sourceType);

    List<ExtractedMessage> findByUserIdAndSourceType(String userId, SourceType sourceType);

    List<ExtractedMessage> findByUserIdAndExtractedAtBetween(
        String userId,
        LocalDateTime start,
        LocalDateTime end
    );

    @Query("SELECT em FROM ExtractedMessage em WHERE em.publishedToKafka = false ORDER BY em.extractedAt ASC")
    List<ExtractedMessage> findUnpublishedMessages();

    @Query("SELECT COUNT(em) FROM ExtractedMessage em WHERE em.userId = :userId AND em.sourceType = :sourceType")
    long countByUserIdAndSourceType(@Param("userId") String userId, @Param("sourceType") SourceType sourceType);
}
