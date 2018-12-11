package com.elvaco.geoservice.repository;

import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.AddressGeoEntity;

import org.springframework.data.repository.CrudRepository;

public interface AddressGeoRepository extends CrudRepository<AddressGeoEntity, Long> {

  AddressGeoEntity findByAddress(Address address);
}
