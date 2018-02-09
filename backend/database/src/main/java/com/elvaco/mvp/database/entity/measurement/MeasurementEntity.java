package com.elvaco.mvp.database.entity.measurement;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Type;

@Entity
@Access(AccessType.FIELD)
@Table(name = "measurement")
public class MeasurementEntity implements Serializable {

  private static final long serialVersionUID = -3650501037709018061L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(nullable = false)
  public Date created;

  @Column(nullable = false)
  public String quantity;

  @ManyToOne
  @JsonBackReference
  public PhysicalMeterEntity physicalMeter;

  @Type(type = "measurement-unit")
  @Column(nullable = false)
  public MeasurementUnit value;

  public MeasurementEntity() {}

  public MeasurementEntity(
    Long id,
    Date created,
    String quantity,
    MeasurementUnit value,
    PhysicalMeterEntity physicalMeter
  ) {
    this.id = id;
    this.created = new Date(created.getTime());
    this.quantity = quantity;
    this.value = value;
    this.physicalMeter = physicalMeter;
  }

  public MeasurementEntity(
    Date created,
    String quantity,
    double value,
    String unit,
    PhysicalMeterEntity physicalMeter
  ) {
    this(null, created, quantity, new MeasurementUnit(unit, value), physicalMeter);
  }
}
