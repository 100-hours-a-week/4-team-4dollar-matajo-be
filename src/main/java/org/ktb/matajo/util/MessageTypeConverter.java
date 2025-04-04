package org.ktb.matajo.util;

import jakarta.persistence.AttributeConverter;
import org.ktb.matajo.entity.MessageType;

public class MessageTypeConverter implements AttributeConverter<MessageType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(MessageType type) {
        return type != null ? (byte) type.getValue() : null;
    }

    @Override
    public MessageType convertToEntityAttribute(Byte value) {
        return value != null ? MessageType.fromValue(value) : null;
    }
}
