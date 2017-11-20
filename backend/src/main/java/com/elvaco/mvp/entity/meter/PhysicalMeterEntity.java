package com.elvaco.mvp.entity.meter;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
public class PhysicalMeterEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public Long organisation_id; /*TODO: Materialize as an organisation entity */
  public String identity;
  public String medium;

  @JsonManagedReference
  @OneToMany(mappedBy = "physicalMeter", fetch = FetchType.LAZY)
  public List<MeasurementEntity> measurements;

  public PhysicalMeterEntity() {
  }

  public PhysicalMeterEntity(Long organisation, String identity, String medium) {
    this.organisation_id = organisation;
    this.identity = identity;
    this.medium = medium;
  }
}
