package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.database.entity.EntityType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
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

  @Enumerated(EnumType.ORDINAL)
  public SeriesDisplayMode seriesDisplayMode;

  @Override
  public Long getId() {
    return id;
  }
}
