package com.elvaco.mvp.dto;

import org.springframework.hateoas.Link;

import java.util.Date;

public class MeasurementDTO {
  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public Date created;
  public Link physicalMeter;
}
