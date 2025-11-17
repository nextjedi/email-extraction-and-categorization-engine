package com.imp.gmail.controller;

import com.imp.gmail.service.GmailMessagesService;
import com.imp.shared.dto.SourceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for Gmail operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailMessagesService gmailMessagesService;

    @GetMapping("/users/{userId}/messages")
    public ResponseEntity<List<SourceMessage>> fetchMessages(
        @PathVariable String userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        log.info("Fetching messages for user {} from {} to {}", userId, from, to);
        List<SourceMessage> messages = gmailMessagesService.fetchMessages(userId, from, to);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/users/{userId}/messages/{messageId}")
    public ResponseEntity<SourceMessage> getMessageById(
        @PathVariable String userId,
        @PathVariable String messageId
    ) {
        log.info("Getting message {} for user {}", messageId, userId);
        SourceMessage message = gmailMessagesService.getMessageById(userId, messageId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/users/{userId}/messages/{messageId}/mark-read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable String userId,
        @PathVariable String messageId
    ) {
        log.info("Marking message {} as read for user {}", messageId, userId);
        gmailMessagesService.markAsRead(userId, messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/available")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable String userId) {
        boolean available = gmailMessagesService.isAvailable(userId);
        return ResponseEntity.ok(available);
    }
}
