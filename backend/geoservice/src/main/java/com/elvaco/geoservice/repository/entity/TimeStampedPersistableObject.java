package com.elvaco.geoservice.repository.entity;

import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class TimeStampedPersistableObject extends PersistableObject {

  @CreatedDate
  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date created;

  @LastModifiedDate
  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date updated;

  public Date getCreated() {
    if (created == null) {
      return null;
    } else {
      return (Date) this.created.clone();
    }
  }

  public void setCreated(Date created) {
    if (created == null) {
      this.created = null;
    } else {
      this.created = (Date) created.clone();
    }
  }

  public Date getUpdated() {
    if (this.updated == null) {
      return null;
    } else {
      return (Date) this.updated.clone();
    }
  }

  public void setUpdated(Date updated) {
    if (updated == null) {
      this.updated = null;
    } else {
      this.updated = (Date) updated.clone();
    }
  }
}
