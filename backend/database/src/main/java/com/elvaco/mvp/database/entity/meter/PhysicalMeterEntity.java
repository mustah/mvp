package com.elvaco.mvp.database.entity.meter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.PrimaryKeyed;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MissingMeasurementEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter")
@ToString(exclude = {"statusLogs", "missingMeasurements", "measurements"})
@Audited
public class PhysicalMeterEntity extends IdentifiableType<UUID> implements PrimaryKeyed {

  private static final long serialVersionUID = 1100904291210178685L;

  @Id
  public UUID id;

  public String address;
  public String externalId;
  public String medium;

  @Nullable
  public String manufacturer;

  @NotAudited
  @JsonManagedReference
  @OneToMany(mappedBy = "id.physicalMeter")
  public Set<MeasurementEntity> measurements = emptySet();

  @NotAudited
  @JsonManagedReference
  @OneToMany(mappedBy = "id.physicalMeter")
  public Set<MissingMeasurementEntity> missingMeasurements = emptySet();

  @NotAudited
  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "pk.physicalMeterId", orphanRemoval = true)
  @Cascade(value = {CascadeType.DELETE, CascadeType.REFRESH})
  public Set<PhysicalMeterStatusLogEntity> statusLogs = new HashSet<>();

  @NotAudited
  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "pk.physicalMeterId", orphanRemoval = true)
  @Cascade(value = {CascadeType.DELETE, CascadeType.REFRESH})
  public Set<MeterAlarmLogEntity> alarms = new HashSet<>();

  @NotAudited
  public LogicalMeterPk logicalMeterPk;

  public long readIntervalMinutes;

  public Integer revision;

  public Integer mbusDeviceType;

  public PhysicalMeterEntity(
    UUID id,
    UUID organisationId,
    String address,
    String externalId,
    String medium,
    @Nullable String manufacturer,
    @Nullable UUID logicalMeterId,
    long readIntervalMinutes,
    @Nullable Integer revision,
    @Nullable Integer mbusDeviceType,
    Set<PhysicalMeterStatusLogEntity> statusLogs,
    Set<MeterAlarmLogEntity> alarms
  ) {
    this.id = id;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterPk = new LogicalMeterPk(logicalMeterId, organisationId);
    this.readIntervalMinutes = readIntervalMinutes;
    this.revision = revision;
    this.mbusDeviceType = mbusDeviceType;
    this.statusLogs = unmodifiableSet(statusLogs);
    this.alarms = unmodifiableSet(alarms);
  }

  @Override
  public UUID getId() {
    return id;
  }

  public UUID getLogicalMeterId() {
    return logicalMeterPk.logicalMeterId;
  }

  public UUID getOrganisationId() {
    return logicalMeterPk.organisationId;
  }

  @Override
  public PrimaryKey primaryKey() {
    return new Pk(id, getOrganisationId());
  }
}
