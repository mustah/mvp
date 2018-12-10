package com.elvaco.mvp.database.entity.measurement;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "missing_measurement")
public class MissingMeasurementEntity extends IdentifiableType<MissingMeasurementPk> {

  private static final long serialVersionUID = -6510696344490444187L;

  @EmbeddedId
  public MissingMeasurementPk id;

  @Override
  public MissingMeasurementPk getId() {
    return id;
  }
}

