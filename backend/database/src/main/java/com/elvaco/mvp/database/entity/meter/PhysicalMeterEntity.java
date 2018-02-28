package com.elvaco.mvp.database.entity.meter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
public class PhysicalMeterEntity extends EntityType<UUID> {

  private static final long serialVersionUID = 1100904291210178685L;

  @Id
  public UUID id;

  @OneToOne
  public OrganisationEntity organisation;

  public String address;
  public String externalId;
  public String medium;
  public String manufacturer;

  @JsonManagedReference
  @OneToMany(mappedBy = "physicalMeter", fetch = FetchType.LAZY)
  public List<MeasurementEntity> measurements;

  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "physicalMeterId", fetch = FetchType.LAZY)
  public Set<PhysicalMeterStatusLogEntity> statusLogs;

  public UUID logicalMeterId;

  public PhysicalMeterEntity() {}

  public PhysicalMeterEntity(
    UUID id,
    OrganisationEntity organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId
  ) {
    this.id = id;
    this.organisation = organisation;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
