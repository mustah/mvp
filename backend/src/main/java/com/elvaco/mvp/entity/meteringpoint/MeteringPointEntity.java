package com.elvaco.mvp.entity.meteringpoint;

import javax.persistence.*;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Type;

import lombok.ToString;

import java.util.List;


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
