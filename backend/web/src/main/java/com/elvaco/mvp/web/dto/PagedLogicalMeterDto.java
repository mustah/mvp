package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
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
  @Nullable
  public String statusChanged;
  public StatusType status;
  @Nullable
  public Double collectionPercentage;
  public UUID organisationId;
  @Nullable
  public String gatewaySerial;
  @Nullable
  public Long readIntervalMinutes;
}
