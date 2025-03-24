package org.ktb.matajo.entity;

public enum MessageType {
    TEXT(1), IMAGE(2), SYSTEM(3);

    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.getValue() == value) return type;
        }
        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
