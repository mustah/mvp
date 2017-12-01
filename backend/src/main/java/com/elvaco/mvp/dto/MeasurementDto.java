package com.elvaco.mvp.dto;

import java.util.Date;
import org.springframework.hateoas.Link;

public class MeasurementDto {
  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public Date created;
  public Link physicalMeter;
}
