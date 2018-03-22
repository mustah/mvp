package com.elvaco.geoservice.repository.entity;

import java.net.URI;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class GeoRequestEntity extends TimeStampedPersistableObject {

  @Embedded
  private Address address;

  private URI callbackUrl;
  private URI errorCallbackUrl;

  public URI getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(URI callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public URI getErrorCallbackUrl() {
    return errorCallbackUrl;
  }

  public void setErrorCallbackUrl(URI errorCallbackUrl) {
    this.errorCallbackUrl = errorCallbackUrl;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

}
