package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "logical_meter")
public class LogicalMeterEntity implements Serializable {

  private static final long serialVersionUID = 5528298891965340483L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Type(type = "property-collection")
  public PropertyCollection propertyCollection;

  @OneToMany(mappedBy = "logicalMeter")
  @JsonManagedReference
  public List<PhysicalMeterEntity> physicalMeters;

  public String status;
  public String medium;

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

  @OneToOne(mappedBy = "logicalMeter", cascade = CascadeType.ALL)
  @JsonManagedReference
  private LocationEntity location;

  public LogicalMeterEntity() {
    this.propertyCollection = new PropertyCollection();
    setLocation(new LocationEntity());
  }

  public LogicalMeterEntity(Long id, Date created, String status) {
    this();
    this.id = id;
    this.created = (Date) created.clone();
    this.status = status;
  }

  public LocationEntity getLocation() {
    return location;
  }

  public void setLocation(LocationEntity location) {
    this.location = location;
    this.location.logicalMeter = this;
  }
}
