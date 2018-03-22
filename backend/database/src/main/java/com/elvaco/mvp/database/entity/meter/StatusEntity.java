package com.elvaco.mvp.database.entity.meter;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;

@Entity
@Access(AccessType.FIELD)
@Table(name = "status")
public class StatusEntity extends EntityType<Long> {

  private static final long serialVersionUID = -124418354565237L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String name;

  public StatusEntity() {}

  public StatusEntity(@Nullable Long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public Long getId() {
    return id;
  }
}
