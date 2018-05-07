package com.elvaco.mvp.web.dto;

import java.util.List;

import static com.elvaco.mvp.web.mapper.SelectionsMapper.GATEWAY_STATUSES;
import static com.elvaco.mvp.web.mapper.SelectionsMapper.MEDIA;
import static com.elvaco.mvp.web.mapper.SelectionsMapper.METER_ALARMS;
import static com.elvaco.mvp.web.mapper.SelectionsMapper.METER_STATUSES;

public class SelectionsDto {

  public final LocationsDto locations;
  public final List<IdNamedDto> gatewayStatuses;
  public final List<IdNamedDto> meterStatuses;
  public final List<IdNamedDto> alarms;
  public final List<IdNamedDto> media;

  public SelectionsDto() {
    this.locations = new LocationsDto();
    this.gatewayStatuses = GATEWAY_STATUSES;
    this.meterStatuses = METER_STATUSES;
    this.alarms = METER_ALARMS;
    this.media = MEDIA;
  }

  public void addLocation(String country, String city, String address) {
    locations.addLocation(country, city, address);
  }
}
