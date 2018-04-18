package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.elvaco.mvp.database.entity.EntityType;

@Entity
class MediumEntity extends EntityType<Long> {

  private static final long serialVersionUID = -8757199707691899529L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String name;

  @Override
  public Long getId() {
    return id;
  }
}
