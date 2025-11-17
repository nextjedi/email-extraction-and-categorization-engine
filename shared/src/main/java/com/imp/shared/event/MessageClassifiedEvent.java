package com.imp.shared.event;

import com.imp.shared.dto.ClassificationResult;
import com.imp.shared.dto.RawMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a message is classified
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageClassifiedEvent {

    private String eventId;
    private RawMessageDTO rawMessage;
    private ClassificationResult classification;
    private LocalDateTime timestamp;
    private String correlationId;
}
