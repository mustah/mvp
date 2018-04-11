package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TO_MVP_UNITS;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageSerializer.deserialize;
import static java.util.stream.Collectors.toList;

@Slf4j
public final class MeteringMessageParser {

  public MeteringMessageDto parse(String message) {
    MeteringMessageDto meteringMessageDto = parseMessage(message, MeteringMessageDto.class)
      .orElseThrow(() -> new MeteringMessageParseException("Failed to parse " + message));

    switch (meteringMessageDto.messageType) {
      case METERING_ALARM_V_1_0:
        return parseAlarmMessage(message).orElseThrow(() -> new MeteringMessageParseException(
          "Failed to parse alarm message: " + message));
      case METERING_MEASUREMENT_V_1_0:
        return parseMeasurementMessage(message).orElseThrow(() -> new MeteringMessageParseException(
          "Failed to parse measurement message: " + message));
      case METERING_METER_STRUCTURE_V_1_0:
        return parseStructureMessage(message).orElseThrow(() -> new MeteringMessageParseException(
          "Failed to parse structure message: " + message));
      default:
        throw new RuntimeException("Unsupported Metering message type: " + meteringMessageDto
          .messageType
          .toString());
    }
  }

  protected Optional<MeteringMeterStructureMessageDto> parseStructureMessage(String message) {
    return parseMessage(message, MeteringMeterStructureMessageDto.class);
  }

  protected Optional<MeteringMeasurementMessageDto> parseMeasurementMessage(String message) {
    return parseMessage(message, MeteringMeasurementMessageDto.class)
      .map(this::translateMeasurementUnits);
  }

  protected Optional<MeteringAlarmMessageDto> parseAlarmMessage(String message) {
    return parseMessage(message, MeteringAlarmMessageDto.class);
  }

  private MeteringMeasurementMessageDto translateMeasurementUnits(
    MeteringMeasurementMessageDto messageDto
  ) {
    return messageDto.withValues(
      messageDto.values
        .stream()
        .map(value -> value.withUnit(METERING_TO_MVP_UNITS.getOrDefault(value.unit, value.unit)))
        .collect(toList()));
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
