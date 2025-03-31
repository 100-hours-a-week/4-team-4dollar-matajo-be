package org.ktb.matajo.entity;

public enum NotificationType {
    CHAT(1),
    TRADE(2),
    SYSTEM(3);

    private final int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NotificationType fromValue(int value) {
        for (NotificationType type : values()) {
            if (type.getValue() == value) return type;
        }
        throw new IllegalArgumentException("Unknown type: " + value);
    }
}