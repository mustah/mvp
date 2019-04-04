package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GatewayDto extends GatewayMandatoryDto {

  public LocationDto location;
  public List<UUID> meterIds;
  public UUID organisationId;

  public GatewayDto(
    UUID id,
    String serial,
    String productModel,
    String status,
    String statusChanged,
    LocationDto location,
    List<UUID> meterIds,
    UUID organisationId,
    JsonNode extraInfo
  ) {
    super(id, productModel, serial, new IdNamedDto(status), statusChanged, null, null, extraInfo);
    this.location = location;
    this.meterIds = meterIds;
    this.organisationId = organisationId;
  }
}
