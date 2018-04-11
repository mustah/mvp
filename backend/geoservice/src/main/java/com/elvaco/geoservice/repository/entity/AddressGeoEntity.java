package com.elvaco.geoservice.repository.entity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {
  @Index(name = "i_street_city_country", columnList = "street,city,country", unique = true)
})
public class AddressGeoEntity extends TimeStampedPersistableObject {

  @Embedded
  private Address address;

  @Embedded
  private GeoLocation geoLocation;

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public GeoLocation getGeoLocation() {
    return geoLocation;
  }

  public void setGeoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
  }
}
