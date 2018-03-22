package com.elvaco.geoservice.repository.entity;

import java.time.ZonedDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class TimeStampedPersistableObject extends PersistableObject {

  @CreatedDate
  private ZonedDateTime created;

  @LastModifiedDate
  private ZonedDateTime updated;

  public ZonedDateTime getCreated() {
    return this.created;

  }

  public void setCreated(ZonedDateTime created) {
    this.created = created;

  }

  public ZonedDateTime getUpdated() {
    return this.updated;
  }

  public void setUpdated(ZonedDateTime updated) {
    this.updated = updated;

  }
}
