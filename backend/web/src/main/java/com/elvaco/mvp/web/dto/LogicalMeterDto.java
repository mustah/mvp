package com.elvaco.mvp.web.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class LogicalMeterDto {
  public Long id;
  public String facility;
  public AddressDto address;
  public IdNamedDto city;
  public GeoPositionDto position;
  public String moid;
  public String sapId;
  public String alarm;
  public List<FlagDto> flags;
  public boolean flagged;
  public String medium;
  public String manufacturer;
  public String statusChanged;
  public List<MeterStatusLogDto> statusChangelog;
  public String created;
  public IdNamedDto status;
  public Long gatewayId;
  public IdNamedDto gatewayStatus;
  public String gatewayProductModel;
}
