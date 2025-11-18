package com.imp.shared.security;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe security context for storing authenticated user information
 */
@Slf4j
public class SecurityContext {

    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

    /**
     * Set the current authenticated user
     */
    public static void setCurrentUser(String userId, String email) {
        currentUserId.set(userId);
        currentUserEmail.set(email);
        log.debug("Security context set for user: {}", userId);
    }

    /**
     * Get the current authenticated user ID
     */
    public static String getCurrentUserId() {
        String userId = currentUserId.get();
        if (userId == null) {
            log.warn("Attempted to get user ID but security context is empty");
            throw new SecurityException("No authenticated user in context");
        }
        return userId;
    }

    /**
     * Get the current authenticated user email
     */
    public static String getCurrentUserEmail() {
        return currentUserEmail.get();
    }

    /**
     * Check if a user is authenticated
     */
    public static boolean isAuthenticated() {
        return currentUserId.get() != null;
    }

    /**
     * Validate that the requested userId matches the authenticated user
     */
    public static void validateUserAccess(String requestedUserId) {
        String currentUser = getCurrentUserId();
        if (!currentUser.equals(requestedUserId)) {
            log.warn("User {} attempted to access data for user {}", currentUser, requestedUserId);
            throw new SecurityException(
                String.format("User %s not authorized to access data for user %s",
                    currentUser, requestedUserId)
            );
        }
    }

    /**
     * Clear the security context (call after request completes)
     */
    public static void clear() {
        currentUserId.remove();
        currentUserEmail.remove();
        log.debug("Security context cleared");
    }
}
