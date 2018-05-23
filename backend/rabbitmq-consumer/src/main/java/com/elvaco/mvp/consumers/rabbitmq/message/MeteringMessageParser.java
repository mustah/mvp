package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;
import java.util.function.Predicate;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TO_MVP_UNITS;
import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.fromJson;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MeteringMessageParser implements MessageParser {

  @Override
  public MeteringMessageDto parse(String message) {
    MeteringMessageDto meteringMessageDto = parseMessage(message, MeteringMessageDto.class)
      .orElseThrow(() -> new FailedToParse("Failed to parse:" + message));

    switch (meteringMessageDto.messageType) {
      case METERING_ALARM_V_1_0:
        return parseAlarmMessage(message)
          .orElseThrow(() ->
            new FailedToParse("Failed to parse alarm message: " + message)
          );
      case METERING_MEASUREMENT_V_1_0:
        return parseMeasurementMessage(message)
          .orElseThrow(() ->
            new FailedToParse("Failed to parse measurement message: " + message)
          );
      case METERING_REFERENCE_INFO_V_1_0:
        return parseReferenceInfoMessage(message)
          .orElseThrow(() ->
            new FailedToParse("Failed to parse reference info message: " + message)
          );
      default:
        throw new RuntimeException("Unsupported Metering message type: "
          + meteringMessageDto.messageType.toString());
    }
  }

  protected Optional<MeteringReferenceInfoMessageDto> parseReferenceInfoMessage(String message) {
    return parseMessage(message, MeteringReferenceInfoMessageDto.class);
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

  private static <T extends MeteringMessageDto> Optional<T> parseMessage(
    String message,
    Class<T> classOfT
  ) {
    try {
      return Optional.ofNullable(fromJson(message, classOfT))
        .filter(isMessageValid(classOfT));
    } catch (JsonSyntaxException ex) {
      log.warn("Failed to parse message of type '{}'", classOfT.getName(), ex);
      return Optional.empty();
    }
  }

  private static <T extends MeteringMessageDto> Predicate<T> isMessageValid(Class<T> classOfT) {
    return (T m) -> {
      try {
        return m.validate();
      } catch (IllegalAccessException ex) {
        log.warn("Illegal access on parsed message of type '{}'", classOfT.getName(), ex);
        return false;
      }
    };
  }

  private static class FailedToParse extends RuntimeException {

    private static final long serialVersionUID = 7334788232403684436L;

    private FailedToParse(String message) {
      super(ellipsize(message));
    }

    private static String ellipsize(String str) {
      return str.length() <= 40 ? str : str.substring(0, 40 - 3) + "...";
    }
  }
}
