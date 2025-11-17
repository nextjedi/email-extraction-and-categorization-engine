package com.imp.shared.strategy;

import com.imp.shared.dto.ClassificationResult;
import com.imp.shared.dto.RawMessageDTO;

/**
 * Strategy interface for classifying messages
 */
public interface ClassificationStrategy {

    /**
     * Classify a raw message
     */
    ClassificationResult classify(RawMessageDTO message);

    /**
     * Get the name of this classification strategy
     */
    String getStrategyName();

    /**
     * Get the priority of this strategy (higher = more important)
     * Used when multiple strategies are available
     */
    default int getPriority() {
        return 0;
    }
}
