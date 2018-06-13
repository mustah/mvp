package com.elvaco.geoservice.repository.entity;

import java.time.Instant;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class TimeStampedPersistableObject extends PersistableObject {

  @CreatedDate
  private Instant created;

  @LastModifiedDate
  private Instant updated;

  public Instant getCreated() {
    return this.created;

  }

  public void setCreated(Instant created) {
    this.created = created;

  }

  public Instant getUpdated() {
    return this.updated;
  }

  public void setUpdated(Instant updated) {
    this.updated = updated;

  }
}
