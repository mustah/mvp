package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;

public interface MeasurementMessageConsumer {

  Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto message);
}
