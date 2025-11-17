package com.imp.extraction.strategy;

import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.ExtractionRequest;
import com.imp.shared.dto.ExtractionResult;
import com.imp.shared.dto.SourceMessage;
import com.imp.shared.strategy.ExtractionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Gmail extraction strategy - delegates to Gmail source service
 */
@Slf4j
@Component
public class GmailExtractionStrategy implements ExtractionStrategy {

    private final WebClient webClient;

    public GmailExtractionStrategy(@Value("${source-services.gmail.url}") String gmailServiceUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(gmailServiceUrl)
            .build();
    }

    @Override
    public ExtractionResult extract(ExtractionRequest request) {
        log.info("Extracting Gmail messages for user {} from {} to {}",
            request.getUserId(), request.getFromDate(), request.getToDate());

        try {
            List<SourceMessage> messages = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/gmail/users/{userId}/messages")
                    .queryParam("from", request.getFromDate())
                    .queryParam("to", request.getToDate())
                    .build(request.getUserId()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<SourceMessage>>() {})
                .block();

            return ExtractionResult.builder()
                .messages(messages != null ? messages : List.of())
                .totalExtracted(messages != null ? messages.size() : 0)
                .hasMore(false)
                .correlationId(request.getCorrelationId())
                .build();

        } catch (Exception e) {
            log.error("Error extracting Gmail messages for user {}", request.getUserId(), e);
            return ExtractionResult.builder()
                .messages(List.of())
                .totalExtracted(0)
                .hasMore(false)
                .correlationId(request.getCorrelationId())
                .build();
        }
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.GMAIL;
    }
}
