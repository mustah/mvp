package com.elvaco.mvp.consumers.rabbitmq.dto;

import javax.annotation.Nullable;

import lombok.ToString;

@ToString(callSuper = true)
public class MeteringStructureMessageDto extends MeteringMessageDto {

  @Nullable
  public final MeterDto meter;

  @Nullable
  public final FacilityDto facility;

  public final String sourceSystemId;
  public final String organisationId;

  @Nullable
  public final GatewayStatusDto gateway;

  public MeteringStructureMessageDto(
    MessageType messageType,
    @Nullable MeterDto meter,
    @Nullable FacilityDto facility,
    String sourceSystemId,
    String organisationId,
    @Nullable GatewayStatusDto gateway
  ) {
    super(messageType);
    this.meter = meter;
    this.facility = facility;
    this.sourceSystemId = sourceSystemId;
    this.organisationId = organisationId;
    this.gateway = gateway;
  }

  public MeteringStructureMessageDto withFacility(FacilityDto facilityDto) {
    return new MeteringStructureMessageDto(
      messageType,
      meter,
      facilityDto,
      sourceSystemId,
      organisationId,
      gateway
    );
  }

  public MeteringStructureMessageDto withMeter(MeterDto meterDto) {
    return new MeteringStructureMessageDto(
      messageType,
      meterDto,
      facility,
      sourceSystemId,
      organisationId,
      gateway
    );
  }

  public MeteringStructureMessageDto withGatewayStatus(GatewayStatusDto gatewayStatusDto) {
    return new MeteringStructureMessageDto(
      messageType,
      meter,
      facility,
      sourceSystemId,
      organisationId,
      gatewayStatusDto
    );
  }
}
