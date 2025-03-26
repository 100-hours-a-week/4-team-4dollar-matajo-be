package org.ktb.matajo.util;

import org.ktb.matajo.entity.MessageType;

import jakarta.persistence.AttributeConverter;

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
