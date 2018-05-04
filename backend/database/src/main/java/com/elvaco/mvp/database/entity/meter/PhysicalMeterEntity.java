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
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
@ToString(exclude = {"measurements", "statusLogs"})
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
  @Cascade(value = CascadeType.MERGE)
  public Set<PhysicalMeterStatusLogEntity> statusLogs;

  public UUID logicalMeterId;

  public long readIntervalMinutes;

  public PhysicalMeterEntity(
    UUID id,
    OrganisationEntity organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId,
    long readIntervalMinutes,
    Set<PhysicalMeterStatusLogEntity> statusLogs
  ) {
    this.id = id;
    this.organisation = organisation;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
    this.readIntervalMinutes = readIntervalMinutes;
    this.statusLogs = unmodifiableSet(statusLogs);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
