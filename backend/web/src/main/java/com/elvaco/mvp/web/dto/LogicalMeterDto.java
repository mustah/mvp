package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class LogicalMeterDto {
  public UUID id;
  public String facility;
  @Nullable
  public String address;
  public LocationDto location;
  public List<FlagDto> flags;
  public boolean flagged;
  public String medium;
  public String manufacturer;
  public String statusChanged;
  public List<MeterStatusLogDto> statusChangelog;
  public String created;
  public Object status;
  @Nullable
  public Double collectionPercentage;
  public List<MeasurementDto> measurements;
  public Long readIntervalMinutes;

  @Nullable
  public GatewayMandatoryDto gateway;
}
