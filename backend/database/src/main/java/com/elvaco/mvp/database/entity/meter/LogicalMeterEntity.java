package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.HashSet;
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

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;

import static java.util.Collections.emptySet;
import static javax.persistence.CascadeType.ALL;

@NoArgsConstructor
@ToString(exclude = "gateways")
@Entity
@Access(AccessType.FIELD)
@Table(name = "logical_meter",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"organisationId", "externalId"})}
)
@Audited
public class LogicalMeterEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = 5528298891965340483L;

  @Id
  public UUID id;

  @OneToMany(mappedBy = "logicalMeterId", fetch = FetchType.EAGER)
  @Fetch(FetchMode.SUBSELECT)
  @Cascade(CascadeType.MERGE)
  public Set<PhysicalMeterEntity> physicalMeters = new HashSet<>();

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
  public ZonedDateTime created;

  @Column(nullable = false)
  public String externalId;

  @Column(nullable = false)
  public UUID organisationId;

  @OneToOne(cascade = ALL, orphanRemoval = true)
  @PrimaryKeyJoinColumn
  @Fetch(FetchMode.JOIN)
  public LocationEntity location;

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
    this.created = created;
    this.physicalMeters = emptySet();
    this.gateways = emptySet();
    this.meterDefinition = meterDefinition;
    this.location = new LocationEntity(id);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
