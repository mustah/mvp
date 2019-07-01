package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "values")
public class MeteringMeasurementMessageDto extends MeteringMessageDto {

  public final IdDto meter;
  public final IdDto facility;
  public final String organisationId;
  public final String sourceSystemId;
  public final List<ValueDto> values;
  @Nullable
  private final IdDto gateway;

  public MeteringMeasurementMessageDto(
    @Nullable IdDto gateway,
    IdDto meter,
    IdDto facility,
    String organisationId,
    String sourceSystemId,
    List<ValueDto> values
  ) {
    super(MessageType.METERING_MEASUREMENT_V_1_0);
    this.gateway = gateway;
    this.meter = meter;
    this.facility = facility;
    this.organisationId = organisationId;
    this.sourceSystemId = sourceSystemId;
    this.values = values;
  }

  public MeteringMeasurementMessageDto withValues(List<ValueDto> values) {
    return new MeteringMeasurementMessageDto(
      gateway,
      meter,
      facility,
      organisationId,
      sourceSystemId,
      values
    );
  }

  public Optional<IdDto> gateway() {
    return Optional.ofNullable(gateway).filter(gatewayIdDto -> nonNull(gatewayIdDto.id));
  }
}
