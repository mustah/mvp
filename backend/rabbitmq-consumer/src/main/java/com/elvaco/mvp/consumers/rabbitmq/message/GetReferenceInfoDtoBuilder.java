package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;

class GetReferenceInfoDtoBuilder {

  private final String organisationId;
  private String meterExternalId;
  private String gatewayExternalId;

  GetReferenceInfoDtoBuilder(String organisationId) {
    this.organisationId = organisationId;
  }

  public void setMeterExternalId(String meterExternalId) {
    this.meterExternalId = meterExternalId;
  }

  public GetReferenceInfoDto build() {
    if (meterExternalId == null && gatewayExternalId == null) {
      return null;
    }
    return new GetReferenceInfoDto(organisationId, meterExternalId, gatewayExternalId);
  }

  public void setGatewayExternalId(String gatewayExternalId) {
    this.gatewayExternalId = gatewayExternalId;
  }
}
