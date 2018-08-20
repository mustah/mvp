package com.elvaco.mvp.database.entity.measurement;

import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "physicalMeter")
@Entity
@Access(AccessType.FIELD)
@Table(
  name = "measurement",
  uniqueConstraints = @UniqueConstraint(columnNames = {"created", "quantity", "physical_meter_id"})
)
public class MeasurementEntity extends IdentifiableType<Long> {

  private static final long serialVersionUID = -3650501037709018061L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public ZonedDateTime created;

  @ManyToOne(optional = false, cascade = javax.persistence.CascadeType.MERGE)
  @JoinColumn(name = "quantity", nullable = false)
  public QuantityEntity quantity;

  @Type(type = "measurement-unit")
  @Column(nullable = false)
  public MeasurementUnit value;

  @JsonBackReference
  @ManyToOne(optional = false)
  @Cascade(CascadeType.MERGE)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(name = "physical_meter_id", nullable = false)
  public PhysicalMeterEntity physicalMeter;

  @Override
  public Long getId() {
    return id;
  }
}
