package com.elvaco.mvp.producers.rabbitmq.dto;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class MeteringMessageDto {

  public final MessageType messageType;

  public final boolean validate() throws IllegalAccessException {
    for (Field field : getClass().getDeclaredFields()) {
      if (field.getAnnotation(Nullable.class) == null && field.get(this) == null) {
        throw new JsonSyntaxException("Field '" + getClass() + "." + field.getName() + "' was not"
          + " initialized");
      }
    }
    return true;
  }
}
