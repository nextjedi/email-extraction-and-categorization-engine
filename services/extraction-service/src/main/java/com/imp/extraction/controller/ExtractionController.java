package com.imp.extraction.controller;

import com.imp.extraction.entity.ExtractedMessage;
import com.imp.extraction.service.ExtractionService;
import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.ExtractionRequest;
import com.imp.shared.dto.ExtractionResult;
import com.imp.shared.util.MessageIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for extraction operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/extraction")
@RequiredArgsConstructor
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/extract")
    public ResponseEntity<ExtractionResult> extractMessages(@RequestBody ExtractionRequest request) {
        log.info("Received extraction request for user {} from source {}",
            request.getUserId(), request.getSourceType());

        if (request.getCorrelationId() == null) {
            request.setCorrelationId(MessageIdGenerator.generateCorrelationId());
        }

        ExtractionResult result = extractionService.extractMessages(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{userId}/messages")
    public ResponseEntity<List<ExtractedMessage>> getUserMessages(
        @PathVariable String userId,
        @RequestParam SourceType sourceType
    ) {
        List<ExtractedMessage> messages = extractionService.getMessagesByUser(userId, sourceType);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/users/{userId}/count")
    public ResponseEntity<Long> getMessageCount(
        @PathVariable String userId,
        @RequestParam SourceType sourceType
    ) {
        long count = extractionService.getMessageCount(userId, sourceType);
        return ResponseEntity.ok(count);
    }
}
