package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
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
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import static java.util.Collections.emptySet;
import static javax.persistence.CascadeType.ALL;

@ToString(exclude = "gateways")
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
  @Fetch(FetchMode.SUBSELECT)
  public Set<PhysicalMeterEntity> physicalMeters;

  @Column(nullable = false)
  public ZonedDateTime created;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "gateways_meters",
    joinColumns = @JoinColumn(name = "logical_meter_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "gateway_id", referencedColumnName = "id")
  )
  @Fetch(FetchMode.SUBSELECT)
  public Set<GatewayEntity> gateways;

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
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.created = ZonedDateTime.ofInstant(created.toInstant(), created.getZone());
    this.physicalMeters = emptySet();
    this.gateways = emptySet();
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
