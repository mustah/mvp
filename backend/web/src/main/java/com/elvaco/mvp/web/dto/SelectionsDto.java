package com.elvaco.mvp.web.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.elvaco.mvp.web.mapper.SelectionsDtoMapper.GATEWAY_STATUSES;
import static com.elvaco.mvp.web.mapper.SelectionsDtoMapper.MEDIA;
import static com.elvaco.mvp.web.mapper.SelectionsDtoMapper.METER_ALARMS;
import static com.elvaco.mvp.web.mapper.SelectionsDtoMapper.METER_STATUSES;

public class SelectionsDto {

  public final LocationsDto locations;
  public final List<IdNamedDto> gatewayStatuses;
  public final List<IdNamedDto> meterStatuses;
  public final List<IdNamedDto> alarms;
  public final List<IdNamedDto> media;
  public final Set<IdNamedDto> facilities;
  public final Set<IdNamedDto> secondaryAddresses;
  public final Set<IdNamedDto> gatewaySerials;

  public SelectionsDto() {
    this.locations = new LocationsDto();
    this.gatewayStatuses = GATEWAY_STATUSES;
    this.meterStatuses = METER_STATUSES;
    this.alarms = METER_ALARMS;
    this.media = MEDIA;
    this.facilities = new HashSet<>();
    this.secondaryAddresses = new HashSet<>();
    this.gatewaySerials = new HashSet<>();
  }

  public void addGateway(IdNamedDto gateway) {
    if (!gatewaySerials.contains(gateway)) {
      gatewaySerials.add(gateway);
    }
  }

  public void addFacility(IdNamedDto facility) {
    if (!facilities.contains(facility)) {
      facilities.add(facility);
    }
  }

  public void addSecondaryAddress(IdNamedDto secondaryAddress) {
    if (!secondaryAddresses.contains(secondaryAddress)) {
      secondaryAddresses.add(secondaryAddress);
    }
  }

  public void addLocation(String country, String city, String address) {
    locations.addLocation(country, city, address);
  }
}
