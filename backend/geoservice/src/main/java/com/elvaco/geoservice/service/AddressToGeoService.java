package com.elvaco.geoservice.service;

import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.GeoLocation;

public interface AddressToGeoService {
  public GeoLocation getGeoByAddress(Address address);

  public String getId();

  public Integer getQuota();

  public Integer getMaxRate();
}
