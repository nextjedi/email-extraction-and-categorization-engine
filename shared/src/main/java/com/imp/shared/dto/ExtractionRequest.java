package com.imp.shared.dto;

import com.imp.shared.constant.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request to extract messages from a source
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequest {

    private String userId;
    private SourceType sourceType;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private String syncToken; // For incremental sync
    private Integer maxResults;

    private Boolean includeAttachments;
    private Boolean markAsRead;

    private String correlationId;
}
