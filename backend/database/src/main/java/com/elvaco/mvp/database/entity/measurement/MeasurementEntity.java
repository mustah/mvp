package com.elvaco.mvp.database.entity.measurement;

import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

@Entity
@Access(AccessType.FIELD)
@Table(
  name = "measurement",
  uniqueConstraints = @UniqueConstraint(columnNames = {"created", "quantity", "physical_meter_id"})
)
@ToString(exclude = "physicalMeter")
public class MeasurementEntity extends IdentifiableType<Long> {

  private static final long serialVersionUID = -3650501037709018061L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public ZonedDateTime created;

  @Column(nullable = false)
  public String quantity;

  @ManyToOne
  @JsonBackReference
  @Cascade(CascadeType.MERGE)
  public PhysicalMeterEntity physicalMeter;

  @Type(type = "measurement-unit")
  @Column(nullable = false)
  public MeasurementUnit value;

  public MeasurementEntity() {}

  public MeasurementEntity(
    Long id,
    ZonedDateTime created,
    String quantity,
    MeasurementUnit value,
    PhysicalMeterEntity physicalMeter
  ) {
    this.id = id;
    this.created = created;
    this.quantity = quantity;
    this.value = value;
    this.physicalMeter = physicalMeter;
  }

  public MeasurementEntity(
    ZonedDateTime created,
    String quantity,
    double value,
    String unit,
    PhysicalMeterEntity physicalMeter
  ) {
    this(null, created, quantity, new MeasurementUnit(unit, value), physicalMeter);
  }

  @Override
  public Long getId() {
    return id;
  }
}
