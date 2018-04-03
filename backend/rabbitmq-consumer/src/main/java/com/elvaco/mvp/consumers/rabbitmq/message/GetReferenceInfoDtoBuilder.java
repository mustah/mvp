package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;

class GetReferenceInfoDtoBuilder {

  private final String organisationId;
  private String meterExternalId;
  private String gatewayExternalId;

  GetReferenceInfoDtoBuilder(String organisationId) {
    this.organisationId = organisationId;
  }

  public Optional<GetReferenceInfoDto> build() {
    if (meterExternalId == null && gatewayExternalId == null) {
      return Optional.empty();
    }
    return Optional.of(new GetReferenceInfoDto(organisationId, meterExternalId, gatewayExternalId));
  }

  void setMeterExternalId(String meterExternalId) {
    this.meterExternalId = meterExternalId;
  }

  void setGatewayExternalId(String gatewayExternalId) {
    this.gatewayExternalId = gatewayExternalId;
  }
}
