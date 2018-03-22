package com.elvaco.geoservice.repository.entity;

import java.net.URI;
import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;

import com.elvaco.geoservice.repository.converter.JpaConverterJson;

@Entity
public class CallbackEntity extends TimeStampedPersistableObject {
  Integer attempt;
  URI callback;
  @Convert(converter = JpaConverterJson.class)
  Object payload;
  LocalDateTime nextRetry;

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

  public LocalDateTime getNextRetry() {
    return nextRetry;
  }

  public void setNextRetry(LocalDateTime nextRetry) {
    this.nextRetry = nextRetry;
  }

  public Integer getAttempt() {
    return attempt;
  }

  public void setAttempt(Integer attempt) {
    this.attempt = attempt;
  }

}
