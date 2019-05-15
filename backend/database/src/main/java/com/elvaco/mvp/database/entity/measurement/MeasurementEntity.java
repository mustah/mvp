package com.elvaco.mvp.database.entity.measurement;

import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Access(AccessType.FIELD)
@Entity
@Table(name = "measurement")
public class MeasurementEntity extends IdentifiableType<MeasurementPk> {

  private static final long serialVersionUID = -3650501037709018061L;

  @EmbeddedId
  public MeasurementPk id;

  @Column
  public ZonedDateTime receivedTime;

  @Column
  public ZonedDateTime expectedTime;

  @Column(nullable = false)
  public double value;

  public MeasurementEntity(
    ZonedDateTime readoutTime,
    ZonedDateTime receivedTime,
    ZonedDateTime expectedTime,
    QuantityEntity quantity,
    double value,
    PhysicalMeterEntity physicalMeter
  ) {
    this.id = new MeasurementPk(readoutTime,
      quantity,
      physicalMeter,
      physicalMeter.getOrganisationId());
    this.value = value;
    this.receivedTime = receivedTime;
    this.expectedTime = expectedTime;
  }

  @Override
  public MeasurementPk getId() {
    return id;
  }
}
