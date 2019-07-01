package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;

public class MeasurementMessageResponseBuilder {

  private final String organisationIdExternal;

  @Nullable
  private String facilityId;

  @Nullable
  private String meterExternalId;

  @Nullable
  private String gatewayExternalId;

  public MeasurementMessageResponseBuilder(String organisationIdExternal) {
    this.organisationIdExternal = organisationIdExternal;
  }

  public MeasurementMessageResponseBuilder meterExternalId(String meterExternalId) {
    this.meterExternalId = meterExternalId;
    return this;
  }

  public MeasurementMessageResponseBuilder gatewayExternalId(String gatewayExternalId) {
    this.gatewayExternalId = gatewayExternalId;
    return this;
  }

  public MeasurementMessageResponseBuilder facilityId(String facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  public Optional<GetReferenceInfoDto> build() {
    if (meterExternalId == null && gatewayExternalId == null && facilityId == null) {
      return Optional.empty();
    } else {
      return Optional.of(GetReferenceInfoDto.builder()
        .organisationId(organisationIdExternal)
        .facility(facilityId != null ? new IdDto(facilityId) : null)
        .meter(meterExternalId != null ? new IdDto(meterExternalId) : null)
        .gateway(gatewayExternalId != null ? new IdDto(gatewayExternalId) : null)
        .build()
      );
    }
  }
}
