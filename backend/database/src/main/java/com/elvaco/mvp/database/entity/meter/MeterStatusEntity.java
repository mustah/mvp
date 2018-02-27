package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter_status")
public class MeterStatusEntity implements Serializable {
  private static final long serialVersionUID = -124418354565237L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String name;

  public MeterStatusEntity() {}

  public MeterStatusEntity(@Nullable Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
