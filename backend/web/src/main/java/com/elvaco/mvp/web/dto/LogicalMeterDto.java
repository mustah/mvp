package com.elvaco.mvp.web.dto;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
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
  public StatusType status;
  public String collectionStatus;

  @Nullable
  public GatewayMandatoryDto gateway;
}
