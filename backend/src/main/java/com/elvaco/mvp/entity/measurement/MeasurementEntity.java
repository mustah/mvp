package com.elvaco.mvp.entity.measurement;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Type;

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

  public MeasurementEntity(long id, Date created, String quantity,
                           MeasurementUnit unit, PhysicalMeterEntity physicalMeter) {
    this.id = id;
    this.created = new Date(created.getTime());
    this.quantity = quantity;
    this.value = unit;
    this.physicalMeter = physicalMeter;
  }

  public MeasurementEntity(Date created, String quantity, double value, String unit,
                           PhysicalMeterEntity physicalMeter) {
    this.created = new Date(created.getTime());
    this.quantity = quantity;
    this.value = new MeasurementUnit(unit, value);
    this.physicalMeter = physicalMeter;
  }
}
