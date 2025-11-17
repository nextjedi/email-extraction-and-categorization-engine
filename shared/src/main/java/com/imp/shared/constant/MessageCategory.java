package com.imp.shared.constant;

/**
 * Classification categories for messages
 */
public enum MessageCategory {
    TRANSACTIONAL("transactional", "Transactional"),
    JOB_SEARCH("job-search", "Job Search"),
    SUBSCRIPTION("subscription", "Subscription"),
    PERSONAL("personal", "Personal"),
    TRAVEL("travel", "Travel"),
    OTHER("other", "Other"),
    UNCLASSIFIED("unclassified", "Unclassified");

    private final String code;
    private final String displayName;

    MessageCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MessageCategory fromCode(String code) {
        for (MessageCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        return OTHER;
    }
}
