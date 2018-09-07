package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MeteringAlarmMessageConsumer implements AlarmMessageConsumer {

  @Override
  public Optional<GetReferenceInfoDto> accept(MeteringAlarmMessageDto message) {
    log.info("Alarm: {}", message);
    log.info("-----------------------------");
    return Optional.empty();
  }
}
