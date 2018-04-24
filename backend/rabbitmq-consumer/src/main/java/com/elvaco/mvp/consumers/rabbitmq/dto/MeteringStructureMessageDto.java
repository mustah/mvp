package com.elvaco.mvp.consumers.rabbitmq.dto;

import lombok.ToString;

@ToString(callSuper = true)
public class MeteringStructureMessageDto extends MeteringMessageDto {

  public final MeterDto meter;
  public final FacilityDto facility;
  public final String sourceSystemId;
  public final String organisationId;
  public final GatewayStatusDto gateway;

  public MeteringStructureMessageDto(
    MessageType messageType,
    MeterDto meter,
    FacilityDto facility,
    String sourceSystemId,
    String organisationId,
    GatewayStatusDto gateway
  ) {
    super(messageType);
    this.meter = meter;
    this.facility = facility;
    this.sourceSystemId = sourceSystemId;
    this.organisationId = organisationId;
    this.gateway = gateway;
  }
}
