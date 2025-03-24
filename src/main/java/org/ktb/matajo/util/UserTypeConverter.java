package org.ktb.matajo.util;

import org.ktb.matajo.entity.UserType;

import jakarta.persistence.AttributeConverter;

public class UserTypeConverter implements AttributeConverter<UserType, Byte> {
  @Override
  public Byte convertToDatabaseColumn(UserType type) {
    return type != null ? (byte) type.getValue() : null;
  }

  @Override
  public UserType convertToEntityAttribute(Byte value) {
    return value != null ? UserType.fromValue(value) : null;
  }
}
