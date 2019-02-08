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
    newSystemMeterDefinition(1, Medium.UNKNOWN_MEDIUM, emptySet());
  public static final MeterDefinition DEFAULT_DISTRICT_HEATING =
    newSystemMeterDefinition(2, Medium.DISTRICT_HEATING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_DISTRICT_COOLING =
    newSystemMeterDefinition(3, Medium.DISTRICT_COOLING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ROOM_SENSOR =
    newSystemMeterDefinition(4, Medium.ROOM_SENSOR, DEFAULT_ROOM_SENSOR_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ELECTRICITY =
    newSystemMeterDefinition(5, Medium.ELECTRICITY, DEFAULT_ELECTRICITY_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_GAS =
    newSystemMeterDefinition(6, Medium.GAS, DEFAULT_GAS_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_HOT_WATER =
    newSystemMeterDefinition(7, Medium.HOT_WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_WATER =
    newSystemMeterDefinition(8, Medium.WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);
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
    String mediumName,
    Set<DisplayQuantity> quantities
  ) {
    // hoho
    //FIXME: These system meter definitions should be provded by a MeterDefinitionProvider
    Medium medium = new Medium(id, mediumName);
    MeterDefinition meterDefinition = new MeterDefinition(
      id,
      null,
      "Default " + mediumName.toLowerCase(),
      medium,
      true,
      quantities
    );
    defaultMeterDefinitions.put(medium, meterDefinition);
    return meterDefinition;
  }
}
