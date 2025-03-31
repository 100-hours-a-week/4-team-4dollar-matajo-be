package org.ktb.matajo.util;

import jakarta.persistence.AttributeConverter;
import org.ktb.matajo.entity.NotificationType;

public class NotificationTypeConverter implements AttributeConverter<NotificationType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(NotificationType type) {
        return type != null ? (byte) type.getValue() : null;
    }

    @Override
    public NotificationType convertToEntityAttribute(Byte value) {
        return value != null ? NotificationType.fromValue(value) : null;
    }
}
