package com.elvaco.mvp.database.entity.measurement;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class MissingMeasurementPk implements Serializable {

  private static final long serialVersionUID = -6135045235245335653L;

  @JsonBackReference
  @ManyToOne(optional = false)
  @JoinColumn(name = "physical_meter_id", updatable = false)
  public PhysicalMeterEntity physicalMeter;

  @Column(nullable = false, updatable = false)
  public ZonedDateTime expectedTime;
}
