package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "logical_meter",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"organisationId", "externalId"})}
)
public class LogicalMeterEntity implements Serializable {

  private static final long serialVersionUID = 5528298891965340483L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @OneToMany(mappedBy = "logicalMeterId", fetch = FetchType.EAGER)
  public Set<PhysicalMeterEntity> physicalMeters;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(nullable = false)
  public Date created;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "gateways_meters",
    joinColumns = @JoinColumn(name = "meter_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "gateway_id", referencedColumnName = "id")
  )
  public List<GatewayEntity> gateways;

  @ManyToOne
  public MeterDefinitionEntity meterDefinition;
  @Column(nullable = false)
  public String externalId;
  @Column(nullable = false)
  public Long organisationId;
  @OneToOne(mappedBy = "logicalMeter", cascade = CascadeType.ALL)
  @JsonManagedReference
  private LocationEntity location;

  public LogicalMeterEntity() {
    this.physicalMeters = Collections.emptySet();
    setLocation(new LocationEntity());
  }

  public LogicalMeterEntity(Long id, String externalId, Long organisationId, Date created) {
    this();
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.created = (Date) created.clone();
  }

  public LocationEntity getLocation() {
    return location;
  }

  public void setLocation(LocationEntity location) {
    this.location = location;
    this.location.logicalMeter = this;
  }
}
