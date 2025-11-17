package com.imp.gmail.client;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.imp.gmail.config.GmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level client that wraps Google Gmail API
 * This is the ONLY class that imports Google's Gmail library
 */
@Slf4j
@Component
public class GmailApiClient {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Intelligent Message Processor";

    private final GmailConfig gmailConfig;

    public GmailApiClient(GmailConfig gmailConfig) {
        this.gmailConfig = gmailConfig;
    }

    /**
     * List messages matching query
     */
    public List<Message> listMessages(String userId, String query, Integer maxResults) {
        try {
            Gmail service = getGmailService(userId);

            Gmail.Users.Messages.List request = service.users().messages().list(userId)
                .setQ(query);

            if (maxResults != null) {
                request.setMaxResults(Long.valueOf(maxResults));
            }

            ListMessagesResponse response = request.execute();

            if (response.getMessages() == null) {
                return new ArrayList<>();
            }

            // Fetch full message details
            List<Message> fullMessages = new ArrayList<>();
            for (Message message : response.getMessages()) {
                Message fullMessage = getMessage(userId, message.getId());
                fullMessages.add(fullMessage);
            }

            return fullMessages;

        } catch (IOException | GeneralSecurityException e) {
            log.error("Error listing Gmail messages for user {}", userId, e);
            throw new RuntimeException("Failed to list Gmail messages", e);
        }
    }

    /**
     * Get a specific message
     */
    public Message getMessage(String userId, String messageId) {
        try {
            Gmail service = getGmailService(userId);
            return service.users().messages()
                .get(userId, messageId)
                .setFormat("full")
                .execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error getting Gmail message {} for user {}", messageId, userId, e);
            throw new RuntimeException("Failed to get Gmail message", e);
        }
    }

    /**
     * Get message attachment
     */
    public MessagePartBody getAttachment(String userId, String messageId, String attachmentId) {
        try {
            Gmail service = getGmailService(userId);
            return service.users().messages().attachments()
                .get(userId, messageId, attachmentId)
                .execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error getting attachment {} for message {} user {}",
                attachmentId, messageId, userId, e);
            throw new RuntimeException("Failed to get attachment", e);
        }
    }

    /**
     * Mark message as read
     */
    public void modifyMessage(String userId, String messageId, List<String> addLabels, List<String> removeLabels) {
        try {
            Gmail service = getGmailService(userId);
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                .setAddLabelIds(addLabels)
                .setRemoveLabelIds(removeLabels);

            service.users().messages()
                .modify(userId, messageId, modifyRequest)
                .execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error modifying Gmail message {} for user {}", messageId, userId, e);
            throw new RuntimeException("Failed to modify Gmail message", e);
        }
    }

    /**
     * Send a message
     */
    public Message sendMessage(String userId, Message message) {
        try {
            Gmail service = getGmailService(userId);
            return service.users().messages()
                .send(userId, message)
                .execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error sending Gmail message for user {}", userId, e);
            throw new RuntimeException("Failed to send Gmail message", e);
        }
    }

    /**
     * Get user's Gmail profile
     */
    public Profile getUserProfile(String userId) {
        try {
            Gmail service = getGmailService(userId);
            return service.users().getProfile(userId).execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error getting Gmail profile for user {}", userId, e);
            throw new RuntimeException("Failed to get Gmail profile", e);
        }
    }

    /**
     * Create Gmail service instance
     * In production, this would use OAuth2 credentials per user
     */
    private Gmail getGmailService(String userId) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // TODO: In production, retrieve user-specific OAuth2 credentials
        // For now, using application default credentials

        return new Gmail.Builder(httpTransport, JSON_FACTORY,
            getCredentials(userId))
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    /**
     * Get credentials for user
     * TODO: Implement proper OAuth2 flow
     */
    private com.google.api.client.http.HttpRequestInitializer getCredentials(String userId) {
        // In production: retrieve OAuth2 credentials from secure storage
        // For demo purposes, returning null (would use Application Default Credentials)
        return null;
    }
}
