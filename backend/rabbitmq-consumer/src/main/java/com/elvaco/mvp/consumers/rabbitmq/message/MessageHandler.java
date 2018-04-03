package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;

public interface MessageHandler {

  Optional<? extends MeteringResponseDto> handle(MeteringMeterStructureMessageDto structureMessage);

  Optional<? extends MeteringResponseDto> handle(MeteringMeasurementMessageDto measurementMessage);

  Optional<? extends MeteringResponseDto> handle(MeteringAlarmMessageDto alarmMessage);
}
