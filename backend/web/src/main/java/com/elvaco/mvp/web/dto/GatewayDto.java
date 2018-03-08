package com.elvaco.mvp.web.dto;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Status;
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
    Status status,
    LocationDto location,
    List<String> meterIds
  ) {
    super(id, productModel, serial, status);
    this.location = location;
    this.meterIds = meterIds;
    this.flags = emptyList();
  }

  public GatewayDto(String id, String serial, String productModel) {
    this(
      id,
      serial,
      productModel,
      Status.OK,
      new LocationDto(),
      emptyList()
    );
  }
}
