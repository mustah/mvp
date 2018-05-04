package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.ToString;

import static java.util.Objects.nonNull;

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

  public Optional<GatewayStatusDto> getGateway() {
    return Optional.ofNullable(gateway).filter(gatewayStatusDto -> nonNull(gatewayStatusDto.id));
  }

  public Optional<MeterDto> getMeter() {
    return Optional.ofNullable(meter).filter(meterDto -> nonNull(meterDto.id));
  }

  public Optional<FacilityDto> getFacility() {
    return Optional.ofNullable(facility).filter(facilityDto -> nonNull(facilityDto.id));
  }
}
