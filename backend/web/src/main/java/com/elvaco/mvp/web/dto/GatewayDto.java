package com.elvaco.mvp.web.dto;

import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;

@ToString
@EqualsAndHashCode
public class GatewayDto {

  public String id;
  public String serial;
  public String productModel;
  public IdNamedDto status;
  public IdNamedDto city;
  public IdNamedDto address;
  public GeoPositionDto position;
  public List<FlagDto> flags;

  public String meterId;
  @Nullable
  public String meterAlarm;
  public String meterManufacturer;
  public IdNamedDto meterStatus;
  public List<String> meterIds;

  public GatewayDto() {}

  public GatewayDto(
    String id,
    String serial,
    String productModel,
    IdNamedDto status,
    IdNamedDto city,
    IdNamedDto address,
    GeoPositionDto position,
    List<FlagDto> flags,
    String meterId,
    @Nullable String meterAlarm,
    String meterManufacturer,
    IdNamedDto meterStatus,
    List<String> meterIds
  ) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
    this.status = status;
    this.city = city;
    this.address = address;
    this.position = position;
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
      IdNamedDto.OK,
      null,
      null,
      new GeoPositionDto(),
      emptyList(),
      null,
      null,
      null,
      null,
      emptyList()
    );
  }
}
