package com.imp.shared.strategy;

import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.ExtractionRequest;
import com.imp.shared.dto.ExtractionResult;

/**
 * Strategy interface for extracting messages from different sources
 */
public interface ExtractionStrategy {

    /**
     * Extract messages according to the request
     */
    ExtractionResult extract(ExtractionRequest request);

    /**
     * Get the source type this strategy handles
     */
    SourceType getSourceType();

    /**
     * Check if this strategy can handle the given request
     */
    default boolean supports(ExtractionRequest request) {
        return getSourceType().equals(request.getSourceType());
    }
}
