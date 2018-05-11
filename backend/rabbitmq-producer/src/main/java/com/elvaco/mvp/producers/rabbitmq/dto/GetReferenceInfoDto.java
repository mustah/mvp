package com.elvaco.mvp.producers.rabbitmq.dto;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetReferenceInfoDto {

  public final String organisationExternalId;

  @Nullable
  public final String meterExternalId;

  @Nullable
  public final String gatewayExternalId;

  @Nullable
  public final String facilityId;
}
