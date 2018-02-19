package com.elvaco.mvp.core.domainmodels;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

@EqualsAndHashCode
public class MeterDefinition {

  public static final MeterDefinition HOT_WATER_METER = new MeterDefinition(
    null,
    "Hot water meter",
    new HashSet<>(
      asList(
        Quantity.VOLUME,
        Quantity.FLOW,
        Quantity.TEMPERATURE
      )),
    true
  );

  public static final MeterDefinition DISTRICT_HEATING_METER = new MeterDefinition(
    null,
    "District heating meter",
    new HashSet<>(asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    )),
    true
  );

  public static final MeterDefinition DISTRICT_COOLING_METER = new MeterDefinition(
    null,
    "District cooling meter",
    new HashSet<>(asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    )),
    true
  );

  @Nullable
  public final Long id;
  public final String medium;
  public final Set<Quantity> quantities;
  public final boolean systemOwned;

  private MeterDefinition(String medium, Set<Quantity> quantities) {
    this(null, medium, quantities, false);
  }

  public MeterDefinition(
    @Nullable Long id,
    String medium,
    Set<Quantity> quantities,
    boolean systemOwned
  ) {
    this.id = id;
    this.medium = medium;
    this.quantities = unmodifiableSet(quantities);
    this.systemOwned = systemOwned;
  }
}
