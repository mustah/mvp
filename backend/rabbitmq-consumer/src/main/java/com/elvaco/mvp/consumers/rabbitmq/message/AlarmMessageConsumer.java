package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

public interface AlarmMessageConsumer {

  Optional<GetReferenceInfoDto> accept(MeteringAlarmMessageDto message);
}
