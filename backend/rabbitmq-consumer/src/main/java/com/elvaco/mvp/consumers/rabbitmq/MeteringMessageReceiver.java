package com.elvaco.mvp.consumers.rabbitmq;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParseException;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeteringMessageReceiver {

  private final MeteringMessageParser parser;
  private final MessageHandler handler;

  public MeteringMessageReceiver(MessageHandler handler) {
    parser = new MeteringMessageParser();
    this.handler = handler;
  }

  public String receiveMessage(byte[] message) {
    String messageStr;
    try {
      messageStr = new String(message, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding!", e);
    }

    log.debug("Received message from Rabbit: {}", messageStr);
    MeteringMessageDto messageDto;
    try {
      messageDto = parser.parse(messageStr);
    } catch (MeteringMessageParseException exception) {
      throw new RuntimeException("Malformed metering message: " + ellipsize(messageStr, 40));
    }

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
    return responseDto.map(MeteringMessageSerializer::serialize)
      .orElse(null);
  }

  private String ellipsize(String str, int maxLength) {
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength - 3) + "...";
  }
}
