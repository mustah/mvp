package com.elvaco.mvp.meteringpoint;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author petjan
 */
@Entity
public class MeteringPoint {

  @Id
  @GeneratedValue
  private Long id;

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
  private int status = 0; // TODO : enumeration?

  /**
   * (Optional) message associated with the status code.
   */
  private String message;

  /**
   * Metering object identifier.
   */
  private String moid;

  // TODO : should probably be a Location object or something later on (inlcuding address and other information)
  private Double latitude;
  private Double longitude;

  public MeteringPoint() {
  }

  public MeteringPoint(String moid) {
    this.moid = moid;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the MOID
   */
  public String getMoid() {
    return moid;
  }

  /**
   * @param moid the MOID to set
   */
  public void setMoid(String moid) {
    this.moid = moid;
  }

  /**
   * @return the latitude
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * @param latitude the latitude to set
   */
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * @param longitude the longitude to set
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
}
