package com.elvaco.mvp.core.domainmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.DisplayQuantity.DEFAULT_DISTRICT_DISPLAY_QUANTITIES;
import static com.elvaco.mvp.core.domainmodels.DisplayQuantity.DEFAULT_ELECTRICITY_DISPLAY_QUANTITIES;
import static com.elvaco.mvp.core.domainmodels.DisplayQuantity.DEFAULT_GAS_DISPLAY_QUANTITIES;
import static com.elvaco.mvp.core.domainmodels.DisplayQuantity.DEFAULT_ROOM_SENSOR_DISPLAY_QUANTITIES;
import static com.elvaco.mvp.core.domainmodels.DisplayQuantity.DEFAULT_WATER_DISPLAY_QUANTITIES;
import static java.util.Collections.emptySet;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class MeterDefinition implements Identifiable<Integer> {

  private static Map<Medium, MeterDefinition> defaultMeterDefinitions = new HashMap<>();

  public static final MeterDefinition UNKNOWN =
    newSystemMeterDefinition(0, Medium.UNKNOWN_MEDIUM, emptySet());
  public static final MeterDefinition DEFAULT_DISTRICT_HEATING =
    newSystemMeterDefinition(1, Medium.DISTRICT_HEATING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_DISTRICT_COOLING =
    newSystemMeterDefinition(2, Medium.DISTRICT_COOLING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ROOM_SENSOR =
    newSystemMeterDefinition(3, Medium.ROOM_SENSOR, DEFAULT_ROOM_SENSOR_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ELECTRICITY =
    newSystemMeterDefinition(4, Medium.ELECTRICITY, DEFAULT_ELECTRICITY_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_GAS =
    newSystemMeterDefinition(5, Medium.GAS, DEFAULT_GAS_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_HOT_WATER =
    newSystemMeterDefinition(6, Medium.HOT_WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_WATER =
    newSystemMeterDefinition(7, Medium.WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);
  public final Integer id;
  @Nullable
  public final Organisation organisation;
  public final String name;
  public final Medium medium;
  public final boolean autoApply;
  public final Set<DisplayQuantity> quantities;

  public static MeterDefinition fromMedium(Medium medium) {
    return defaultMeterDefinitions.getOrDefault(medium, UNKNOWN);
  }

  @Override
  public Integer getId() {
    return id;
  }

  private static MeterDefinition newSystemMeterDefinition(
    int id,
    Medium medium,
    Set<DisplayQuantity> quantities
  ) {
    // hoho
    //FIXME: These system meter definitions should be provded by a MeterDefinitionProvider
    MeterDefinition meterDefinition = new MeterDefinition(
      id,
      null,
      "Default " + medium.name.toLowerCase(),
      medium,
      true,
      quantities
    );
    defaultMeterDefinitions.put(medium, meterDefinition);
    return meterDefinition;
  }
}
