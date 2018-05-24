package com.elvaco.mvp.consumers.rabbitmq.message;

import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.producers.rabbitmq.MessageSerializer;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MeteringMessageListener implements MessageListener {

  private final MessageParser messageParser;
  private final MeasurementMessageConsumer measurementMessageConsumer;
  private final ReferenceInfoMessageConsumer referenceInfoMessageConsumer;

  @Nullable
  @Override
  public String onMessage(String message) {
    MeteringMessageDto meteringMessage = messageParser.parse(message);

    try {
      return handleMessage(meteringMessage);
    } catch (RuntimeException exception) {
      log.warn("Message handling raised exception. Offending message is: {}", message);
      throw exception;
    }
  }

  private String handleMessage(MeteringMessageDto meteringMessage) {
    if (meteringMessage instanceof MeteringMeasurementMessageDto) {
      return measurementMessageConsumer.accept((MeteringMeasurementMessageDto) meteringMessage)
        .map(MessageSerializer::toJson)
        .orElse(null);
    } else if (meteringMessage instanceof MeteringReferenceInfoMessageDto) {
      referenceInfoMessageConsumer.accept((MeteringReferenceInfoMessageDto) meteringMessage);
      return null;
    } else if (meteringMessage instanceof MeteringAlarmMessageDto) {
      log.warn("Ignoring unhandled Alarm message");
      return null;
    } else {
      throw new RuntimeException("Unknown message type: " + meteringMessage.getClass().getName());
    }
  }
}
