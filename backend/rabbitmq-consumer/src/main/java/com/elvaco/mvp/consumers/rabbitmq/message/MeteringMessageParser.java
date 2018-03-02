package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeteringMessageParser {

  private final Gson gson;

  public MeteringMessageParser() {
    gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();
  }

  Optional<MeteringMeterStructureMessageDto> parseStructureMessage(String message) {
    return parseMessage(message, MeteringMeterStructureMessageDto.class);
  }

  Optional<MeteringMeasurementMessageDto> parseMeasurementMessage(String message) {
    return parseMessage(message, MeteringMeasurementMessageDto.class);
  }

  public MeteringMessageDto parse(String message) {
    MeteringMessageDto meteringMessageDto = parseMessage(
      message,
      MeteringMessageDto.class
    ).orElseThrow(() -> new MeteringMessageParseException("Failed to parse " + message));

    Class<? extends MeteringMessageDto> classOfT;
    switch (meteringMessageDto.messageType) {
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
