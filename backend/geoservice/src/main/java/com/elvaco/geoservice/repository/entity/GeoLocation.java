package com.elvaco.geoservice.repository.entity;

import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class GeoLocation {

  private String longitude;
  private String latitude;
  private Double confidence;
  private String source;
}
