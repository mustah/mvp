package com.elvaco.mvp.entity.meteringpoint;

import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.ToString;
import org.hibernate.annotations.Type;

@ToString
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

  public MeteringPointEntity() {}
}
