package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

public class MeteringMeasurementMessageDto extends MeteringMessageDto {

  @Nullable
  private final GatewayIdDto gateway;
  public final MeterIdDto meter;
  public final FacilityIdDto facility;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<ValueDto> values;

  public MeteringMeasurementMessageDto(
    MessageType messageType,
    @Nullable GatewayIdDto gateway,
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

  public MeteringMeasurementMessageDto withValues(List<ValueDto> values) {
    return new MeteringMeasurementMessageDto(
      messageType,
      gateway,
      meter,
      facility,
      organisationId,
      sourceSystemId,
      values
    );
  }

  public Optional<GatewayIdDto> gateway() {
    return Optional.ofNullable(gateway).filter(gatewayIdDto -> gatewayIdDto.id != null);
  }
}
