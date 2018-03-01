package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.lang.reflect.Field;

import com.google.gson.JsonSyntaxException;

public class MeteringMessageDto {
  public final MessageType messageType;

  public MeteringMessageDto(MessageType messageType) {
    this.messageType = messageType;
  }

  public final boolean validate() throws IllegalAccessException {
    for (Field field : getClass().getDeclaredFields()) {
      if (field.get(this) == null) {
        throw new JsonSyntaxException("Field " + field.getName() + " was not initialized");
      }
    }
    return true;
  }
}
