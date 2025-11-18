package com.imp.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Audit logger for tracking all sensitive operations
 */
@Slf4j
@Component
public class AuditLogger {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Log a security event
     */
    public void logSecurityEvent(AuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("AUDIT: {}", json);

            // In production, also send to:
            // - Dedicated audit database
            // - SIEM system
            // - CloudWatch/Stackdriver
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }

    /**
     * Log message access
     */
    public void logMessageAccess(String userId, String messageId, String action, boolean success) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action(action)
            .resource("message:" + messageId)
            .result(success ? "SUCCESS" : "FAILURE")
            .build();

        logSecurityEvent(event);
    }

    /**
     * Log authentication attempt
     */
    public void logAuthenticationAttempt(String userId, String ipAddress, boolean success) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("AUTHENTICATION")
            .ipAddress(ipAddress)
            .result(success ? "SUCCESS" : "FAILURE")
            .build();

        logSecurityEvent(event);
    }

    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(String userId, String requestedUserId, String resource) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("UNAUTHORIZED_ACCESS_ATTEMPT")
            .resource(resource)
            .metadata("attempted_user_id: " + requestedUserId)
            .result("BLOCKED")
            .severity("HIGH")
            .build();

        logSecurityEvent(event);
    }

    /**
     * Log data export
     */
    public void logDataExport(String userId, String format) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("DATA_EXPORT")
            .metadata("format: " + format)
            .result("SUCCESS")
            .severity("MEDIUM")
            .build();

        logSecurityEvent(event);
    }

    /**
     * Log data deletion
     */
    public void logDataDeletion(String userId, String reason) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("DATA_DELETION")
            .metadata("reason: " + reason)
            .result("SUCCESS")
            .severity("HIGH")
            .build();

        logSecurityEvent(event);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditEvent {
        private LocalDateTime timestamp;
        private String userId;
        private String action;
        private String resource;
        private String ipAddress;
        private String userAgent;
        private String result;
        private String metadata;
        private String severity;
    }
}
