package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.Optional;

import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;

public class MeasurementMessageResponseBuilder {

  private final String organisationIdExternal;

  private String facilityId;
  private String meterExternalId;
  private String gatewayExternalId;

  public MeasurementMessageResponseBuilder(String organisationIdExternal) {
    this.organisationIdExternal = organisationIdExternal;
  }

  public void setMeterExternalId(String meterExternalId) {
    this.meterExternalId = meterExternalId;
  }

  public void setGatewayExternalId(String gatewayExternalId) {
    this.gatewayExternalId = gatewayExternalId;
  }

  public void setFacilityId(String facilityId) {
    this.facilityId = facilityId;
  }

  public Optional<GetReferenceInfoDto> build() {
    if (meterExternalId == null && gatewayExternalId == null && facilityId == null) {
      return Optional.empty();
    } else {
      return Optional.of(new GetReferenceInfoDto(
        organisationIdExternal,
        null,
        meterExternalId != null ? new MeterIdDto(meterExternalId) : null,
        gatewayExternalId != null ? new GatewayIdDto(gatewayExternalId) : null,
        facilityId != null ? new FacilityIdDto(facilityId) : null
      ));
    }
  }

}
