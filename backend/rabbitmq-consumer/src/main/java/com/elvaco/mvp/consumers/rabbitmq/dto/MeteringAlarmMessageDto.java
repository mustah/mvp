package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;

import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public class MeteringAlarmMessageDto extends MeteringMessageDto {

  public final IdDto meter;
  public final IdDto facility;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<AlarmDto> alarm;

  public MeteringAlarmMessageDto(
    IdDto meter,
    IdDto facility,
    String organisationId,
    String sourceSystemId,
    List<AlarmDto> alarm
  ) {
    super(MessageType.METERING_ALARM_V_1_0);
    this.meter = meter;
    this.facility = facility;
    this.organisationId = organisationId;
    this.sourceSystemId = sourceSystemId;
    this.alarm = alarm;
  }
}

