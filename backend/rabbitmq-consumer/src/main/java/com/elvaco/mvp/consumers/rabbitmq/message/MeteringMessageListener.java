package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.core.util.MessageThrottler;
import com.elvaco.mvp.producers.rabbitmq.MessageSerializer;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RequiredArgsConstructor
public class MeteringMessageListener implements MessageListener {

  private static final Logger MESSAGE_LOGGER = LoggerFactory.getLogger("MeteringMessageLogger");
  private static final Logger OUTGOING_MESSAGE_LOGGER = LoggerFactory.getLogger(
    "OutgoingMessageLogger");

  private final MessageParser messageParser;
  private final MeasurementMessageConsumer measurementMessageConsumer;
  private final ReferenceInfoMessageConsumer referenceInfoMessageConsumer;
  private final AlarmMessageConsumer alarmMessageConsumer;
  private final InfrastructureMessageConsumer infrastructureStatusMessageConsumer;
  private final MessageThrottler<String, GetReferenceInfoDto> referenceInfoThrottler;

  @Nullable
  @Override
  public String onMessage(String message) {
    try {
      MESSAGE_LOGGER.info(message);
      return messageParser.parse(message)
        .map(this::handleMessage)
        .map(
          msg -> {
            OUTGOING_MESSAGE_LOGGER.info(msg);
            return msg;
          }
        )
        .orElse(null);
    } catch (RuntimeException exception) {
      log.warn("Message handling raised exception. Offending message is: {}", message);
      throw exception;
    }
  }

  @Nullable
  private String handleMessage(MeteringMessageDto meteringMessage) {
    if (meteringMessage instanceof MeteringMeasurementMessageDto) {
      long start = System.nanoTime();
      String response = measurementMessageConsumer
        .accept((MeteringMeasurementMessageDto) meteringMessage)
        .filter(this::throttleAndLog)
        .map(MessageSerializer::toJson)
        .orElse(null);
      stopAndLog("Measurement", start);
      return response;
    } else if (meteringMessage instanceof MeteringReferenceInfoMessageDto) {
      long start = System.nanoTime();
      referenceInfoMessageConsumer.accept((MeteringReferenceInfoMessageDto) meteringMessage);
      stopAndLog("Reference Info", start);
      return null;
    } else if (meteringMessage instanceof MeteringAlarmMessageDto) {
      return alarmMessageConsumer
        .accept((MeteringAlarmMessageDto) meteringMessage)
        .filter(this::throttleAndLog)
        .map(MessageSerializer::toJson)
        .orElse(null);
    } else if (meteringMessage instanceof InfrastructureStatusMessageDto) {
      long start = System.nanoTime();
      infrastructureStatusMessageConsumer.accept((InfrastructureStatusMessageDto) meteringMessage);
      stopAndLog("Infrastructure status", start);
      return null;
    } else {
      throw new RuntimeException("Unknown message type: " + meteringMessage.getClass().getName());
    }
  }

  private boolean throttleAndLog(GetReferenceInfoDto getReferenceInfoDto) {
    boolean throttle = referenceInfoThrottler.throttle(getReferenceInfoDto);
    if (throttle) {
      log.debug("Throttling Get-Reference message {}", getReferenceInfoDto);
    }
    return !throttle;
  }

  private void stopAndLog(String message, long start) {
    long elapsedTime = System.nanoTime() - start;
    long millis = TimeUnit.NANOSECONDS.toMillis(elapsedTime);
    if (millis >= 30) {
      log.debug(message + ": {} ms", millis);
    }
  }
}
