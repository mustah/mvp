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
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "quantity")
public class QuantityEntity extends EntityType<Long> {

  private static final long serialVersionUID = -8628799320716504900L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public String unit;

  public QuantityEntity(@Nullable Long id, String name, String unit) {
    this.id = id;
    this.name = name;
    this.unit = unit;
  }

  @Override
  public Long getId() {
    return id;
  }
}
