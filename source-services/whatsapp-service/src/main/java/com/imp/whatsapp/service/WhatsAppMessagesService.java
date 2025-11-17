package com.imp.whatsapp.service;

import com.imp.shared.api.SourceMessagesApi;
import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.SourceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WhatsApp messages service
 * TODO: Integrate with actual WhatsApp Business API or WhatsApp Web API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppMessagesService implements SourceMessagesApi {

    @Override
    public List<SourceMessage> fetchMessages(String userId, LocalDateTime from, LocalDateTime to) {
        log.info("Fetching WhatsApp messages for user {} from {} to {}", userId, from, to);
        // TODO: Implement actual WhatsApp API integration
        return new ArrayList<>();
    }

    @Override
    public IncrementalFetchResult fetchIncremental(String userId, String syncToken) {
        return new IncrementalFetchResult(new ArrayList<>(), UUID.randomUUID().toString(), false);
    }

    @Override
    public SourceMessage getMessageById(String userId, String sourceMessageId) {
        // TODO: Implement
        return null;
    }

    @Override
    public String sendMessage(String userId, SendMessageRequest request) {
        // TODO: Implement
        return UUID.randomUUID().toString();
    }

    @Override
    public void markAsRead(String userId, String sourceMessageId) {
        // TODO: Implement
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.WHATSAPP;
    }

    @Override
    public boolean isAvailable(String userId) {
        // TODO: Implement availability check
        return false;
    }
}
