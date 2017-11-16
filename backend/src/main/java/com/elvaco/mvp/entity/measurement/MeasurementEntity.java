package com.elvaco.mvp.entity.measurement;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;

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

  @Transient
  public double value;
  @Transient
  public String unit;

  @ManyToOne
  @JsonBackReference
  public PhysicalMeterEntity physicalMeter;

  @Column(name = "value")
  @Access(AccessType.PROPERTY)
  private String getMeasurementValue() {
    return String.format("%f %s", value, unit);
  }

  @Column(name = "value")
  @Access(AccessType.PROPERTY)
  private void setMeasurementValue(String measurementValue) {
    int i = measurementValue.lastIndexOf(' ');
    String[] parts = {measurementValue.substring(0, i), measurementValue.substring(i + 1)};
    try {
      value = Double.parseDouble(parts[0]);
    } catch (NumberFormatException ex) {
      value = Double.NaN;
    }
    unit = parts[1];
  }

  public MeasurementEntity() {
  }

  public MeasurementEntity(Date created, String quantity, double value, String unit, PhysicalMeterEntity physicalMeter) {
    this.created = created;
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.physicalMeter = physicalMeter;
  }
}
