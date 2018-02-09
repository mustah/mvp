package com.elvaco.mvp.web.dto;

import java.util.Date;
import javax.annotation.Nullable;

public class MeasurementDto {

  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public Date created;
  @Nullable
  public PhysicalMeterDto physicalMeter;
}
