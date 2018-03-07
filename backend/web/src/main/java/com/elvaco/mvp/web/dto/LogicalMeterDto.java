package com.elvaco.mvp.web.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class LogicalMeterDto {
  public String id;
  public String facility;
  public String sapId;
  public String alarm;
  public LocationDto location;
  public List<FlagDto> flags;
  public boolean flagged;
  public String medium;
  public String manufacturer;
  public String statusChanged;
  public List<MeterStatusLogDto> statusChangelog;
  public String created;
  public IdNamedDto status;
  public String gatewayId;
  public IdNamedDto gatewayStatus;
  public String gatewayProductModel;
  public String gatewaySerial;
}
