package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.util.Optional;

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
        meterExternalId,
        gatewayExternalId,
        facilityId
      ));
    }
  }
}
