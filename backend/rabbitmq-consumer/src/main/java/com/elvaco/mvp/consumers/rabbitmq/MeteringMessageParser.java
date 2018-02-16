package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MeteringMessageParser {
  private final Gson gson;

  MeteringMessageParser() {
    gson = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();
  }

  Optional<MeteringMeterStructureMessageDto> parseStructureMessage(String message) {
    return parseMessage(message, MeteringMeterStructureMessageDto.class);
  }

  Optional<MeteringMeasurementMessageDto> parseMeasurementMessage(String message) {
    return parseMessage(message, MeteringMeasurementMessageDto.class);
  }

  private <T extends MeteringMessageDto> Optional<T> parseMessage(
    String message,
    Class<T> classOfT
  ) {
    try {
      return Optional.ofNullable(gson.fromJson(message, classOfT))
        .filter((T m) -> {
          try {
            return m.validate();
          } catch (IllegalAccessException ex) {
            log.warn(String.format(
              "Illegal access on parsed message of type '%s'",
              classOfT.getName()
            ), ex);
            return false;
          }
        });
    } catch (JsonSyntaxException ex) {
      log.warn(String.format("Failed to parse message of type '%s'", classOfT.getName()), ex);
      return Optional.empty();
    }
  }
}
