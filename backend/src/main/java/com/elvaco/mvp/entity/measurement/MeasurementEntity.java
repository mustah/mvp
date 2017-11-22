package com.elvaco.mvp.entity.measurement;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Access(AccessType.FIELD)
@Table(name = "measurement")
public class MeasurementEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @Temporal(value = TemporalType.TIMESTAMP)
  public Date created;
  public String quantity;

  @ManyToOne
  @JsonBackReference
  public PhysicalMeterEntity physicalMeter;

  @Type(type = "measurement-unit")
  public MeasurementUnit value;


  public MeasurementEntity() {
  }

  public MeasurementEntity(Date created, String quantity, double value, String unit, PhysicalMeterEntity physicalMeter) {
    this.created = created;
    this.quantity = quantity;
    this.value = new MeasurementUnit(unit, value);
    this.physicalMeter = physicalMeter;
  }
}
