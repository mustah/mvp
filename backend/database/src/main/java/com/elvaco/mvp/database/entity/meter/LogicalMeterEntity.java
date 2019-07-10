package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.gateway.GatewayMeterEntity;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import static javax.persistence.CascadeType.ALL;

@NoArgsConstructor
@ToString(exclude = "gatewayMeters")
@Entity
@Access(AccessType.FIELD)
@Table(name = "logical_meter",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"organisationId", "externalId"})}
)
@Audited
public class LogicalMeterEntity extends IdentifiableType<EntityPk> {

  private static final long serialVersionUID = 5528298891965340483L;

  @EmbeddedId
  public EntityPk pk;

  @OneToMany(mappedBy = "logicalMeterPk", fetch = FetchType.EAGER)
  public Set<PhysicalMeterEntity> physicalMeters = new HashSet<>();

  @OneToMany(mappedBy = "logicalMeter", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
  @NotAudited
  public Set<GatewayMeterEntity> gatewayMeters = new HashSet<>();

  @ManyToOne(optional = false)
  public MeterDefinitionEntity meterDefinition;

  @Column(nullable = false)
  public ZonedDateTime created;

  @Column(nullable = false)
  public String externalId;

  @OneToOne(cascade = ALL, orphanRemoval = true)
  @PrimaryKeyJoinColumn
  @Fetch(FetchMode.JOIN)
  public LocationEntity location;
  
  @Column
  public String utcOffset;

  public LogicalMeterEntity(
    EntityPk pk,
    String externalId,
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition,
    String utcOffset
  ) {
    this.pk = pk;
    this.externalId = externalId;
    this.created = created;
    this.meterDefinition = meterDefinition;
    this.location = LocationEntity.builder().pk(pk).build();
    this.utcOffset = utcOffset;
  }

  @Override
  public EntityPk getId() {
    return pk;
  }

  public UUID getLogicalMeterId() {
    return pk.id;
  }

  public UUID getOrganisationId() {
    return pk.organisationId;
  }
}
