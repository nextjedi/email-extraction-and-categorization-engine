package com.imp.shared.event;

import com.imp.shared.dto.RawMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a message is extracted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageExtractedEvent {

    private String eventId;
    private RawMessageDTO message;
    private LocalDateTime timestamp;
    private String correlationId;
}
