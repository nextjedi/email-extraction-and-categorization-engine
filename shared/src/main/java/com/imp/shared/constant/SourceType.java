package com.imp.shared.constant;

/**
 * Enumeration of supported message sources
 */
public enum SourceType {
    GMAIL("gmail", "Gmail"),
    WHATSAPP("whatsapp", "WhatsApp"),
    TELEGRAM("telegram", "Telegram"),
    SMS("sms", "SMS");

    private final String code;
    private final String displayName;

    SourceType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SourceType fromCode(String code) {
        for (SourceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown source type: " + code);
    }
}
