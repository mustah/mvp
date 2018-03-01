package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;

public interface MeteringMessageHandler {

  void handle(MeteringMeterStructureMessageDto structureMessage);

  void handle(MeteringMeasurementMessageDto measurementMessage);
}
