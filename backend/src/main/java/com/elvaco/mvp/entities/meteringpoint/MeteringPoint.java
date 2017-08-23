package com.elvaco.mvp.entities.meteringpoint;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class MeteringPoint {

  @Id
  @GeneratedValue
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

  // TODO : should probably be a Location object or something later on (inlcuding address and other information)
  public Double latitude;
  public Double longitude;

  public MeteringPoint() {}

  public MeteringPoint(String moid) {
    this.moid = moid;
  }
}
