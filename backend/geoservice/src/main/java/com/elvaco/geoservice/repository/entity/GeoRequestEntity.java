package com.elvaco.geoservice.repository.entity;

import java.net.URI;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import com.elvaco.geoservice.repository.converter.JpaConverterUri;

@Entity
public class GeoRequestEntity extends TimeStampedPersistableObject {

  @Embedded
  private Address address;

  @Convert(converter = JpaConverterUri.class)
  @Column(length = 1024)
  private URI callbackUrl;

  @Convert(converter = JpaConverterUri.class)
  @Column(length = 1024)
  private URI errorCallbackUrl;

  private boolean force = false;

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

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }
}
