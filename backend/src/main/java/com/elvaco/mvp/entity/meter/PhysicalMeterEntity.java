package com.elvaco.mvp.entity.meter;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
public class PhysicalMeterEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public Long organisationId; /*TODO: Materialize as an organisation entity */
  public String identity;
  public String medium;

  @JsonManagedReference
  @OneToMany(mappedBy = "physicalMeter", fetch = FetchType.LAZY)
  public List<MeasurementEntity> measurements;

  @ManyToOne
  @JsonBackReference
  public MeteringPointEntity meteringPoint;

  public PhysicalMeterEntity() {
  }

  public PhysicalMeterEntity(Long organisation, String identity, String medium) {
    this.organisationId = organisation;
    this.identity = identity;
    this.medium = medium;
  }

  public void setMeteringPoint(MeteringPointEntity meteringPoint) {
    this.meteringPoint = meteringPoint;
  }
}
