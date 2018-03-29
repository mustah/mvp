package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageSerializer.deserialize;

@Slf4j
public final class MeteringMessageParser {

  Optional<MeteringMeterStructureMessageDto> parseStructureMessage(String message) {
    return parseMessage(message, MeteringMeterStructureMessageDto.class);
  }

  Optional<MeteringMeasurementMessageDto> parseMeasurementMessage(String message) {
    return parseMessage(message, MeteringMeasurementMessageDto.class);
  }

  Optional<MeteringAlarmMessageDto> parseAlarmMessage(String message) {
    return parseMessage(message, MeteringAlarmMessageDto.class);
  }

  public MeteringMessageDto parse(String message) {
    MeteringMessageDto meteringMessageDto = parseMessage(
      message,
      MeteringMessageDto.class
    ).orElseThrow(() -> new MeteringMessageParseException("Failed to parse " + message));

    Class<? extends MeteringMessageDto> classOfT;
    switch (meteringMessageDto.messageType) {
      case METERING_ALARM_V_1_0:
        classOfT = MeteringAlarmMessageDto.class;
        break;
      case METERING_MEASUREMENT_V_1_0:
        classOfT = MeteringMeasurementMessageDto.class;
        break;
      case METERING_METER_STRUCTURE_V_1_0:
        classOfT = MeteringMeterStructureMessageDto.class;
        break;
      default:
        throw new RuntimeException("Unsupported Metering message type: " + meteringMessageDto
          .messageType
          .toString());
    }
    return parseMessage(message, classOfT)
      .orElseThrow(() -> new MeteringMessageParseException(
        "Failed to parse message of type '" + classOfT.getName() + "': " + message));
  }

  private <T extends MeteringMessageDto> Optional<T> parseMessage(
    String message,
    Class<T> classOfT
  ) {
    try {
      return Optional.ofNullable(deserialize(message, classOfT))
        .filter((T m) -> {
          try {
            return m.validate();
          } catch (IllegalAccessException ex) {
            log.warn("Illegal access on parsed message of type '{}'", classOfT.getName(), ex);
            return false;
          }
        });
    } catch (JsonSyntaxException ex) {
      log.warn("Failed to parse message of type '{}'", classOfT.getName(), ex);
      return Optional.empty();
    }
  }
}
