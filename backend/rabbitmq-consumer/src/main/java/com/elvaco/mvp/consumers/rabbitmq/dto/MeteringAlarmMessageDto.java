package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;

import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class MeteringAlarmMessageDto extends MeteringMessageDto {

  public final GatewayIdDto gateway;
  public final MeterIdDto meter;
  public final FacilityIdDto facility;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<AlarmDto> alarm;

  public MeteringAlarmMessageDto(
    GatewayIdDto gateway,
    MeterIdDto meter,
    FacilityIdDto facility,
    String organisationId,
    String sourceSystemId,
    List<AlarmDto> alarm
  ) {
    super(MessageType.METERING_ALARM_V_1_0);
    this.gateway = gateway;
    this.meter = meter;
    this.facility = facility;
    this.organisationId = organisationId;
    this.sourceSystemId = sourceSystemId;
    this.alarm = alarm;
  }
}

