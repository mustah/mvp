package com.elvaco.geoservice.repository.entity;

import java.net.URI;
import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;

import com.elvaco.geoservice.repository.converter.JpaConverterJson;

@Entity
public class CallbackEntity extends TimeStampedPersistableObject {
  Integer attempt;
  URI callback;
  @Convert(converter = JpaConverterJson.class)
  Object payload;
  Date nextRetry;

  public URI getCallback() {
    return callback;
  }

  public void setCallback(URI callback) {
    this.callback = callback;
  }

  public Object getPayload() {
    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public Date getNextRetry() {
    if (nextRetry == null) {
      return null;
    } else {
      return (Date)nextRetry.clone();
    }
  }

  public void setNextRetry(Date nextRetry) {
    if (nextRetry != null) {
      this.nextRetry = (Date) nextRetry.clone();
    } else {
      this.nextRetry = null;
    }
  }

  public Integer getAttempt() {
    return attempt;
  }

  public void setAttempt(Integer attempt) {
    this.attempt = attempt;
  }

}
