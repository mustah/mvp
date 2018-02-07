package com.elvaco.mvp.core.domainmodels;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeterDefinition {
  public static final MeterDefinition HOT_WATER_METER = new MeterDefinition(
    "Hot water meter",
    Arrays.asList(
      Quantity.VOLUME,
      Quantity.FLOW,
      Quantity.TEMPERATURE
    )
  );

  public static final MeterDefinition DISTRICT_HEATING_METER = new MeterDefinition(
    "District heating meter",
    Arrays.asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    )
  );

  public static final MeterDefinition DISTRICT_COOLING_METER = new MeterDefinition(
    "District cooling meter",
    Arrays.asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    )
  );

  @Nullable
  public final Long id;
  private String medium;
  private List<Quantity> quantities;

  private MeterDefinition(String medium, List<Quantity> quantities) {
    this(null, medium, quantities);
  }

  public MeterDefinition(
    @Nullable Long id,
    String medium,
    List<Quantity> quantities
  ) {
    this.id = id;
    this.medium = medium;
    this.quantities = quantities;
  }

  public String getMedium() {
    return medium;
  }

  public List<Quantity> getQuantities() {
    return quantities;
  }
}
