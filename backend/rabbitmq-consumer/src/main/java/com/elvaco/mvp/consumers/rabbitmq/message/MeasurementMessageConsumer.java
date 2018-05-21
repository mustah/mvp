package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

@FunctionalInterface
public interface MeasurementMessageConsumer {

  Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto message);
}
