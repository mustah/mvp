package com.elvaco.mvp.core.domainmodels;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.DISTRICT_COOLING_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.DISTRICT_HEATING_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.HOT_WATER_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.UNKNOWN_METER_TYPE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public enum Medium {

  DISTRICT_HEATING("District heating", DISTRICT_HEATING_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return DISTRICT_QUANTITIES;
    }
  },

  DISTRICT_COOLING("District cooling", DISTRICT_COOLING_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return DISTRICT_QUANTITIES;
    }
  },

  HEAT_RETURN_TEMP("Heat, Return temp", DISTRICT_HEATING_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return DISTRICT_QUANTITIES;
    }
  },

  HOT_WATER("Hot water", HOT_WATER_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return HEAT_WATER_QUANTITIES;
    }
  },

  UNKNOWN_MEDIUM("Unknown medium", UNKNOWN_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return emptySet();
    }
  };

  private static final Set<Quantity> DISTRICT_QUANTITIES = unmodifiableSet(new HashSet<>(asList(
    Quantity.ENERGY,
    Quantity.VOLUME,
    Quantity.POWER,
    Quantity.VOLUME_FLOW,
    Quantity.FORWARD_TEMPERATURE,
    Quantity.RETURN_TEMPERATURE,
    Quantity.DIFFERENCE_TEMPERATURE
  )));

  private static final Set<Quantity> HEAT_WATER_QUANTITIES = unmodifiableSet(new HashSet<>(asList(
    Quantity.VOLUME,
    Quantity.VOLUME_FLOW,
    Quantity.TEMPERATURE
  )));

  public final String medium;
  public final MeterDefinitionType meterDefinitionType;

  Medium(String medium, MeterDefinitionType meterDefinitionType) {
    this.medium = medium;
    this.meterDefinitionType = meterDefinitionType;
  }

  protected abstract Set<Quantity> quantities();

  public static Medium from(String medium) {
    return Stream.of(values())
      .filter(m -> m.medium.equalsIgnoreCase(medium))
      .findFirst()
      .orElse(UNKNOWN_MEDIUM);
  }
}
