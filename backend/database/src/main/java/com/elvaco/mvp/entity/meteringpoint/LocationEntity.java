package com.elvaco.mvp.entity.meteringpoint;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "location")
@Access(AccessType.FIELD)
@EqualsAndHashCode(exclude = "meteringPoint")
public class LocationEntity implements Serializable {

  private static final long serialVersionUID = -6244183552379157552L;

  @Id
  @OneToOne
  @JoinColumn(name = "meter_id")
  @JsonBackReference
  public MeteringPointEntity meteringPoint;

  public String country;
  public String city;
  public String streetAddress;
  public Double latitude;
  public Double longitude;
  public Double confidence;

  public boolean hasCoordinates() {
    return latitude != null && longitude != null && confidence != null;
  }
}
