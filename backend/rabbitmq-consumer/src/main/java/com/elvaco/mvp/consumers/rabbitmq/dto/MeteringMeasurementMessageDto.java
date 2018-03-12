package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;

public class MeteringMeasurementMessageDto extends MeteringMessageDto {

  public final GatewayIdDto gateway;
  public final MeterIdDto meter;
  public final FacilityIdDto facility;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<ValueDto> values;

  public MeteringMeasurementMessageDto(
    MessageType messageType,
    GatewayIdDto gateway,
    MeterIdDto meter,
    FacilityIdDto facility,
    String organisationId,
    String sourceSystemId,
    List<ValueDto> values
  ) {
    super(messageType);
    this.gateway = gateway;
    this.meter = meter;
    this.facility = facility;
    this.organisationId = organisationId;
    this.sourceSystemId = sourceSystemId;
    this.values = values;
  }
}
