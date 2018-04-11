package com.elvaco.mvp.consumers.rabbitmq;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParseException;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MeteringMessageReceiver {

  private static final String UTF_8 = "UTF-8";
  private static final int MAX_LENGTH = 40;
  private static final MeteringMessageParser MESSAGE_PARSER = new MeteringMessageParser();

  private final MessageHandler handler;

  @Nullable
  public String receiveMessage(byte[] rawMessage) {
    String message = encodedMessage(rawMessage);

    log.debug("Received message from Rabbit: {}", message);

    MeteringMessageDto messageDto = parseMessage(message);

    Optional<? extends MeteringResponseDto> responseDto;
    if (messageDto instanceof MeteringAlarmMessageDto) {
      responseDto = handler.handle((MeteringAlarmMessageDto) messageDto);
    } else if (messageDto instanceof MeteringMeasurementMessageDto) {
      responseDto = handler.handle((MeteringMeasurementMessageDto) messageDto);
    } else if (messageDto instanceof MeteringMeterStructureMessageDto) {
      responseDto = handler.handle((MeteringMeterStructureMessageDto) messageDto);
    } else {
      throw new RuntimeException("Unknown message type: " + messageDto.getClass().getName());
    }
    return responseDto.map(MeteringMessageSerializer::serialize).orElse(null);
  }

  private MeteringMessageDto parseMessage(String message) {
    try {
      return MESSAGE_PARSER.parse(message);
    } catch (MeteringMessageParseException exception) {
      throw new RuntimeException("Malformed metering message: " + ellipsize(message));
    }
  }

  private static String encodedMessage(byte[] message) {
    try {
      return new String(message, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding!", e);
    }
  }

  private static String ellipsize(String str) {
    if (str.length() <= MAX_LENGTH) {
      return str;
    }
    return str.substring(0, MAX_LENGTH - 3) + "...";
  }
}
