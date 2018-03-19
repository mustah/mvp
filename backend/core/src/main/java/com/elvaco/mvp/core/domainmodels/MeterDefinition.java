package com.elvaco.mvp.core.domainmodels;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@EqualsAndHashCode
@ToString
public class MeterDefinition implements Identifiable<MeterDefinitionType> {

  public static final MeterDefinition UNKNOWN_METER = new MeterDefinition(
    MeterDefinitionType.UNKNOWN_METER_TYPE,
    "Unknown meter",
    emptySet(),
    true
  );

  public static final MeterDefinition HOT_WATER_METER = new MeterDefinition(
    MeterDefinitionType.HOT_WATER_METER_TYPE,
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
    MeterDefinitionType.DISTRICT_HEATING_METER_TYPE,
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
    MeterDefinitionType.DISTRICT_COOLING_METER_TYPE,
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

  public final MeterDefinitionType type;
  public final String medium;
  public final Set<Quantity> quantities;
  public final boolean systemOwned;

  public MeterDefinition(
    MeterDefinitionType type,
    String medium,
    Set<Quantity> quantities,
    boolean systemOwned
  ) {
    this.type = type;
    this.medium = medium;
    this.quantities = unmodifiableSet(quantities);
    this.systemOwned = systemOwned;
  }

  @Override
  public MeterDefinitionType getId() {
    return type;
  }
}
