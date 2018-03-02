package com.elvaco.mvp.database.entity.meter;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static javax.persistence.CascadeType.ALL;

@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "logical_meter",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"organisationId", "externalId"})}
)
public class LogicalMeterEntity extends EntityType<UUID> {

  private static final long serialVersionUID = 5528298891965340483L;

  @Id
  public UUID id;

  @OneToMany(mappedBy = "logicalMeterId", fetch = FetchType.EAGER)
  public Set<PhysicalMeterEntity> physicalMeters;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(nullable = false)
  public Date created;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "gateways_meters",
    joinColumns = @JoinColumn(name = "logical_meter_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "gateway_id", referencedColumnName = "id")
  )
  public List<GatewayEntity> gateways;

  @ManyToOne(optional = false)
  public MeterDefinitionEntity meterDefinition;

  @Column(nullable = false)
  public String externalId;

  @Column(nullable = false)
  public UUID organisationId;

  @OneToOne(cascade = ALL)
  @PrimaryKeyJoinColumn
  public LocationEntity location;

  public LogicalMeterEntity() {}

  public LogicalMeterEntity(
    UUID id,
    String externalId,
    UUID organisationId,
    Date created,
    MeterDefinitionEntity meterDefinition
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.created = (Date) created.clone();
    this.physicalMeters = emptySet();
    this.gateways = emptyList();
    this.meterDefinition = meterDefinition;
    setLocation(new LocationEntity(id));
  }

  public LocationEntity getLocation() {
    return location;
  }

  public void setLocation(LocationEntity location) {
    this.location = location;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
