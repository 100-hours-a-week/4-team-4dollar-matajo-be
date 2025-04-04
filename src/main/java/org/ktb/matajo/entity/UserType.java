package org.ktb.matajo.entity;

public enum UserType {
    USER(1), KEEPER(2);

    private int value;

    UserType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserType fromValue(int value) {
        for (UserType type : values()) {
            if (type.getValue() == value) return type;
        }
        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
