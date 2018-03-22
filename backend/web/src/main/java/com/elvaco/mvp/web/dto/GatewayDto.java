package com.elvaco.mvp.web.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static java.util.Collections.emptyList;

@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GatewayDto extends GatewayMandatoryDto {

  public LocationDto location;
  public List<String> meterIds;
  public List<FlagDto> flags;

  public GatewayDto(
    String id,
    String serial,
    String productModel,
    String status,
    String statusChanged,
    LocationDto location,
    List<String> meterIds
  ) {
    super(id, productModel, serial, status, statusChanged);
    this.location = location;
    this.meterIds = meterIds;
    this.flags = emptyList();
  }
}
