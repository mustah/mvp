package com.elvaco.geoservice.service;

import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.GeoLocation;

public interface AddressToGeoService {

  GeoLocation getGeoByAddress(Address address);

  String getId();

  Integer getQuota();

  Integer getMaxRate();
}
