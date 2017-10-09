package com.elvaco.mvp.entity.meteringpoint;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.ToString;


@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "mps")
public class MeteringPointEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /**
   * Status codes.
   * <pre>
   * 0         : Everything is oki doki
   * 100 - 199 : Information
   * 200 - 299 : Warning from Collection
   * 300 - 399 : Error from Collection
   * 400 - 499 : Warning from Validation (a.k.a. threshold warnings)
   * 500 - 599 : Error from Validation (a.k.a. threshold alarms)
   * </pre>
   */
  public int status = 0; // TODO : enumeration?

  /**
   * (Optional) message associated with the status code.
   */
  public String message;

  /**
   * Metering object identifier.
   */
  public String moid;

  @Type(type = "property-collection")
  public MvpPropertyCollection propertyCollection;

  public MeteringPointEntity() {}

  public MeteringPointEntity(String moid) {
    this.moid = moid;
  }
}
