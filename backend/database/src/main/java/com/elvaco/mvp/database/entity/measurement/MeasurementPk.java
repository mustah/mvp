package com.elvaco.mvp.database.entity.measurement;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class MeasurementPk implements Serializable {

  private static final long serialVersionUID = 5534347183934651569L;

  @Column(nullable = false, updatable = false)
  public ZonedDateTime readoutTime;

  @ManyToOne(optional = false, cascade = javax.persistence.CascadeType.MERGE)
  @JoinColumn(name = "quantity_id", nullable = false, updatable = false)
  public QuantityEntity quantity;

  @JsonBackReference
  @ManyToOne(optional = false)
  @Cascade(CascadeType.MERGE)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(name = "physical_meter_id", nullable = false, updatable = false)
  public PhysicalMeterEntity physicalMeter;

  @Column(nullable = false, updatable = false)
  public UUID organisationId;

}
