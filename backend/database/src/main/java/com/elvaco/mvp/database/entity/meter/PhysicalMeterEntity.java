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
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MissingMeasurementEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
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
public class PhysicalMeterEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = 1100904291210178685L;

  @Id
  public UUID id;

  @OneToOne
  public OrganisationEntity organisation;

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
  @OneToMany(mappedBy = "physicalMeterId", orphanRemoval = true)
  @Cascade(value = {CascadeType.DELETE, CascadeType.REFRESH})
  public Set<PhysicalMeterStatusLogEntity> statusLogs = new HashSet<>();

  @NotAudited
  @OrderBy("stop desc, start desc")
  @OneToMany(mappedBy = "physicalMeterId", orphanRemoval = true)
  @Cascade(value = {CascadeType.DELETE, CascadeType.REFRESH})
  public Set<MeterAlarmLogEntity> alarms = new HashSet<>();

  public UUID logicalMeterId;

  public long readIntervalMinutes;

  public PhysicalMeterEntity(
    UUID id,
    OrganisationEntity organisation,
    String address,
    String externalId,
    String medium,
    @Nullable String manufacturer,
    @Nullable UUID logicalMeterId,
    long readIntervalMinutes,
    Set<PhysicalMeterStatusLogEntity> statusLogs,
    Set<MeterAlarmLogEntity> alarms
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
    this.alarms = unmodifiableSet(alarms);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
