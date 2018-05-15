package com.elvaco.mvp.consumers.rabbitmq.dto;

import javax.annotation.Nullable;

import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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
    @Nullable MeterDto meter,
    @Nullable FacilityDto facility,
    String sourceSystemId,
    String organisationId,
    @Nullable GatewayStatusDto gateway
  ) {
    super(MessageType.METERING_METER_STRUCTURE_V_1_0);
    this.meter = meter;
    this.facility = facility;
    this.sourceSystemId = sourceSystemId;
    this.organisationId = organisationId;
    this.gateway = gateway;
  }

  public MeteringStructureMessageDto withFacility(FacilityDto facilityDto) {
    return new MeteringStructureMessageDto(
      meter,
      facilityDto,
      sourceSystemId,
      organisationId,
      gateway
    );
  }

  public MeteringStructureMessageDto withMeter(MeterDto meterDto) {
    return new MeteringStructureMessageDto(
      meterDto,
      facility,
      sourceSystemId,
      organisationId,
      gateway
    );
  }

  public MeteringStructureMessageDto withGatewayStatus(GatewayStatusDto gatewayStatusDto) {
    return new MeteringStructureMessageDto(
      meter,
      facility,
      sourceSystemId,
      organisationId,
      gatewayStatusDto
    );
  }
}
