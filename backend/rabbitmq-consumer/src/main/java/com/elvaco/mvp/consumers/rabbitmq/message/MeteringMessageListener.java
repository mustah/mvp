package com.elvaco.mvp.consumers.rabbitmq.message;

import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MeteringMessageListener implements MessageListener {

  private final MessageParser messageParser;
  private final MeasurementMessageConsumer measurementMessageConsumer;
  private final StructureMessageConsumer structureMessageConsumer;

  @Nullable
  @Override
  public String onMessage(String message) {
    MeteringMessageDto meteringMessage = messageParser.parse(message);

    if (meteringMessage instanceof MeteringMeasurementMessageDto) {
      return measurementMessageConsumer.accept((MeteringMeasurementMessageDto) meteringMessage)
        .map(MessageSerializer::toJson)
        .orElse(null);
    } else if (meteringMessage instanceof MeteringStructureMessageDto) {
      structureMessageConsumer.accept((MeteringStructureMessageDto) meteringMessage);
      return null;
    } else {
      throw new RuntimeException("Unknown message type: " + meteringMessage.getClass().getName());
    }
  }
}
