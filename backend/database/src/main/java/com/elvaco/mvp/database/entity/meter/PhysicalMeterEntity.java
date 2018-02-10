package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;

@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
@EqualsAndHashCode(exclude = {"measurements"})
public class PhysicalMeterEntity implements Serializable {

  private static final long serialVersionUID = 1100904291210178685L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @OneToOne
  public OrganisationEntity organisation;

  public String identity;
  public String medium;

  @JsonManagedReference
  @OneToMany(mappedBy = "physicalMeter", fetch = FetchType.LAZY)
  public List<MeasurementEntity> measurements;

  public Long logicalMeterId;

  public PhysicalMeterEntity() {}

  public PhysicalMeterEntity(OrganisationEntity organisation, String identity, String medium) {
    this(null, organisation, identity, medium, null);
  }

  public PhysicalMeterEntity(
    Long id,
    OrganisationEntity organisation,
    String identity,
    String medium,
    Long logicalMeterId
  ) {
    this.id = id;
    this.organisation = organisation;
    this.identity = identity;
    this.medium = medium;
    this.logicalMeterId = logicalMeterId;
  }

}
