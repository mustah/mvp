package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;

public class MeteringMeasurementMessageDto extends MeteringMessageDto {

  public final GatewayStatusDto gateway;
  public final MeterStatusDto meter;
  public final String facilityId;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<ValueDto> values;
  public final List<AlarmDto> alarms;

  public MeteringMeasurementMessageDto(
    MessageType messageType,
    GatewayStatusDto gateway,
    MeterStatusDto meter,
    String facilityId,
    String organisationId,
    String sourceSystemId,
    List<ValueDto> values,
    List<AlarmDto> alarms
  ) {
    super(messageType);
    this.gateway = gateway;
    this.meter = meter;
    this.facilityId = facilityId;
    this.organisationId = organisationId;
    this.sourceSystemId = sourceSystemId;
    this.values = values;
    this.alarms = alarms;
  }
}
