package com.imp.classification.repository;

import com.imp.classification.entity.ClassifiedMessage;
import com.imp.shared.constant.MessageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassifiedMessageRepository extends JpaRepository<ClassifiedMessage, Long> {

    Optional<ClassifiedMessage> findByMessageId(String messageId);

    List<ClassifiedMessage> findByPrimaryCategory(MessageCategory category);

    List<ClassifiedMessage> findByUserIdAndPrimaryCategory(String userId, MessageCategory category);

    long countByPrimaryCategory(MessageCategory category);
}
