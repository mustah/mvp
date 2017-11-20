package com.elvaco.mvp.entity.meteringpoint;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import lombok.ToString;


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

  public MeteringPointEntity() {}
}
