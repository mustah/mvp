package com.elvaco.mvp.core.domainmodels;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.COLD_WATER_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.DISTRICT_COOLING_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.DISTRICT_HEATING_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.ELECTRICITY_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.GAS_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.HOT_WATER_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.ROOM_TEMP_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.UNKNOWN_METER_TYPE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinitionType.WATER_METER_TYPE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
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
      return WATER_QUANTITIES;
    }
  },

  WATER("Water", WATER_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return WATER_QUANTITIES;
    }
  },

  COLD_WATER("Cold water", COLD_WATER_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return WATER_QUANTITIES;
    }
  },

  ROOM_TEMP("Room sensor", ROOM_TEMP_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return ROOM_TEMP_QUANTITIES;
    }
  },

  ELECTRICITY("Electricity", ELECTRICITY_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return ELECTRICITY_QUANTITIES;
    }
  },

  UNKNOWN_MEDIUM("Unknown medium", UNKNOWN_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return emptySet();
    }
  },

  GAS("Gas", GAS_METER_TYPE) {
    @Override
    protected Set<Quantity> quantities() {
      return GAS_QUANTITIES;
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

  private static final Set<Quantity> GAS_QUANTITIES = singleton(Quantity.VOLUME);

  private static final Set<Quantity> WATER_QUANTITIES = unmodifiableSet(new HashSet<>(asList(
    Quantity.VOLUME
  )));

  private static final Set<Quantity> ELECTRICITY_QUANTITIES = unmodifiableSet(new HashSet<>(asList(
    Quantity.ENERGY,
    Quantity.ENERGY_RETURN,
    Quantity.REACTIVE_ENERGY,
    Quantity.POWER
  )));

  private static final Set<Quantity> ROOM_TEMP_QUANTITIES = unmodifiableSet(new HashSet<>(asList(
    Quantity.TEMPERATURE,
    Quantity.HUMIDITY
  )));

  public final String medium;
  public final MeterDefinitionType meterDefinitionType;

  Medium(String medium, MeterDefinitionType meterDefinitionType) {
    this.medium = medium;
    this.meterDefinitionType = meterDefinitionType;
  }

  public static Medium from(String medium) {
    return Stream.of(values())
      .filter(m -> m.medium.equalsIgnoreCase(medium))
      .findFirst()
      .orElse(UNKNOWN_MEDIUM);
  }

  protected abstract Set<Quantity> quantities();
}
