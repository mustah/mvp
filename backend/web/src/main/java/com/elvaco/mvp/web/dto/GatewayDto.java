package com.elvaco.mvp.web.dto;

import java.util.List;
import javax.annotation.Nullable;

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
  public List<FlagDto> flags;

  public String meterId;
  @Nullable
  public String meterAlarm;
  public String meterManufacturer;
  public Status meterStatus;
  public List<String> meterIds;

  public GatewayDto(
    String id,
    String serial,
    String productModel,
    Status status,
    LocationDto location,
    List<FlagDto> flags,
    String meterId,
    @Nullable String meterAlarm,
    String meterManufacturer,
    Status meterStatus,
    List<String> meterIds
  ) {
    super(id, productModel, serial, status);
    this.location = location;
    this.flags = flags;
    this.meterId = meterId;
    this.meterAlarm = meterAlarm;
    this.meterManufacturer = meterManufacturer;
    this.meterStatus = meterStatus;
    this.meterIds = meterIds;
  }

  public GatewayDto(String id, String serial, String productModel) {
    this(
      id,
      serial,
      productModel,
      Status.OK,
      new LocationDto(),
      emptyList(),
      null,
      null,
      null,
      Status.UNKNOWN,
      emptyList()
    );
  }
}
