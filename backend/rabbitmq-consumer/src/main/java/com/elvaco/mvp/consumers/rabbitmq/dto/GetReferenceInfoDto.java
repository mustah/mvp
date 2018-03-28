package com.elvaco.mvp.consumers.rabbitmq.dto;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class GetReferenceInfoDto implements MeteringResponseDto {

  private String organisationExternalId;
  @Nullable
  private String meterExternalId;
  @Nullable
  private String gatewayExternalId;

  public GetReferenceInfoDto(
    String organisationExternalId,
    @Nullable String meterExternalId,
    @Nullable String gatewayExternalId
  ) {
    this.organisationExternalId = organisationExternalId;
    this.meterExternalId = meterExternalId;
    this.gatewayExternalId = gatewayExternalId;
  }
}
