package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PagedLogicalMeterDto {

  public UUID id;
  public String facility;
  @Nullable
  public String address;
  public LocationDto location;
  public String medium;
  public String manufacturer;
  public boolean isReported;
  @Nullable
  public Double collectionPercentage;
  public UUID organisationId;
  @Nullable
  public String gatewaySerial;
  @Nullable
  public Long readIntervalMinutes;
  @Nullable
  public AlarmDto alarm;
}
