package com.elvaco.mvp.entity.meteringpoint;

import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.elvaco.mvp.entity.gateway.GatewayEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "metering_point")
public class MeteringPointEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /**
   * Metering object identifier.
   */
  @Type(type = "property-collection")
  public PropertyCollection propertyCollection;

  @OneToMany(mappedBy = "meteringPoint")
  @JsonManagedReference
  public List<PhysicalMeterEntity> physicalMeters;

  public String status;
  public String medium;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "gateways_meters",
      joinColumns = @JoinColumn(name = "meter_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "gateway_id", referencedColumnName = "id")
  )
  public List<GatewayEntity> gateways;

  public MeteringPointEntity() {
  }
}
