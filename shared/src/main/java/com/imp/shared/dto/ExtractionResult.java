package com.imp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of message extraction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {

    @Builder.Default
    private List<SourceMessage> messages = new ArrayList<>();

    private String nextSyncToken; // For incremental sync
    private Integer totalExtracted;
    private Boolean hasMore;

    private String correlationId;
}
