package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;

public interface MessageHandler {

  void handle(MeteringMeterStructureMessageDto structureMessage);

  void handle(MeteringMeasurementMessageDto measurementMessage);

  void handle(MeteringAlarmMessageDto alarmMessage);
}
