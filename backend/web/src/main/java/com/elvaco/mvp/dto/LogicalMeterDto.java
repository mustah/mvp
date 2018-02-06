package com.elvaco.mvp.dto;

import java.util.List;

public class LogicalMeterDto extends LocationDto {
  public Long id;
  public String moid;
  public String sapId;
  public Long measurementId;
  public String facility;
  public String alarm;
  public List<FlagDto> flags;
  public boolean flagged;
  public String medium;
  public String manufacturer;
  public String statusChanged;
  public List<MeterStatusChangeLogDto> statusChangelog;
  public String created;
  public IdNamedDto status;
  public Long gatewayId;
  public IdNamedDto gatewayStatus;
  public String gatewayProductModel;
}
