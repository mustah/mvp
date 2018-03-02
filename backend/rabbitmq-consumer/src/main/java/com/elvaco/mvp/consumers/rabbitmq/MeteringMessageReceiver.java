package com.elvaco.mvp.consumers.rabbitmq;

import java.io.UnsupportedEncodingException;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeteringMessageReceiver {

  private final MeteringMessageParser parser;
  private final MessageHandler handler;

  public MeteringMessageReceiver(MessageHandler handler) {
    parser = new MeteringMessageParser();
    this.handler = handler;
  }

  public void receiveMessage(byte[] message) {
    String messageStr;
    try {
      messageStr = new String(message, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding!", e);
    }

    log.debug("Received message from Rabbit: {}", messageStr);
    try {
      MeteringMessageDto messageDto = parser.parse(messageStr);
      if (messageDto instanceof MeteringMeterStructureMessageDto) {
        handler.handle((MeteringMeterStructureMessageDto) messageDto);
      } else if (messageDto instanceof MeteringMeasurementMessageDto) {
        handler.handle((MeteringMeasurementMessageDto) messageDto);
      } else {
        throw new RuntimeException("Unknown message type: " + messageDto.getClass().getName());
      }
    } catch (MeteringMessageParser.MeteringMessageParseException e) {
      throw new RuntimeException(
        "Malformed metering message: " + ellipsize(messageStr, 40), e);
    }


  }

  private String ellipsize(String str, int maxLength) {
    if (str.length() <= maxLength) {
      return str;
    }

    return str.substring(0, maxLength - 3) + "...";
  }
}
