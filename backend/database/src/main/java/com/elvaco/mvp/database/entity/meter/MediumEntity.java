package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
class MediumEntity {
  @Id
  @GeneratedValue
  public Long id;
  public String name;
}
