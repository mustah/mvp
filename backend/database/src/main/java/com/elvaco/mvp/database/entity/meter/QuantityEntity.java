package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;

@Entity
@Table(name = "quantity")
@EqualsAndHashCode
public class QuantityEntity implements Serializable {
  private static final long serialVersionUID = -8628799320716504900L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Nullable
  public Long id;
  public String name;
  public String unit;

  public QuantityEntity() {}

  public QuantityEntity(@Nullable Long id, String name, String unit) {
    this.id = id;
    this.name = name;
    this.unit = unit;
  }
}
