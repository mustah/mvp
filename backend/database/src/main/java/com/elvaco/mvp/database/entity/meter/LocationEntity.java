package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "location")
@Access(AccessType.FIELD)
public class LocationEntity extends EntityType<Long> {

  private static final long serialVersionUID = -6244183552379157552L;

  @Id
  @OneToOne
  @JoinColumn(name = "meter_id")
  @JsonBackReference
  public LogicalMeterEntity logicalMeter;

  public String country;
  public String city;
  public String streetAddress;
  public Double latitude;
  public Double longitude;
  public Double confidence;

  public boolean hasCoordinates() {
    return latitude != null && longitude != null && confidence != null;
  }

  @Override
  public Long getId() {
    return logicalMeter.getId();
  }
}
