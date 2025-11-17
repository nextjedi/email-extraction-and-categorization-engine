package com.imp.sms.service;

import com.imp.shared.api.SourceMessagesApi;
import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.SourceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SMS messages service
 * Can be integrated with Twilio, AWS SNS, or other SMS providers
 */
@Slf4j
@Service
public class SmsMessagesService implements SourceMessagesApi {

    @Override
    public List<SourceMessage> fetchMessages(String userId, LocalDateTime from, LocalDateTime to) {
        log.info("Fetching SMS messages for user {} from {} to {}", userId, from, to);
        return new ArrayList<>();
    }

    @Override
    public IncrementalFetchResult fetchIncremental(String userId, String syncToken) {
        return new IncrementalFetchResult(new ArrayList<>(), UUID.randomUUID().toString(), false);
    }

    @Override
    public SourceMessage getMessageById(String userId, String sourceMessageId) {
        return null;
    }

    @Override
    public String sendMessage(String userId, SendMessageRequest request) {
        return UUID.randomUUID().toString();
    }

    @Override
    public void markAsRead(String userId, String sourceMessageId) {
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.SMS;
    }

    @Override
    public boolean isAvailable(String userId) {
        return false;
    }
}
