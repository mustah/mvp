package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder(toBuilder = true)
public class MeterDefinition implements Identifiable<Long> {

  public static final MeterDefinition UNKNOWN =
    newSystemMeterDefinition(Medium.UNKNOWN_MEDIUM, emptySet());
  public static final MeterDefinition DEFAULT_DISTRICT_HEATING =
    newSystemMeterDefinition(Medium.DISTRICT_HEATING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_DISTRICT_COOLING =
    newSystemMeterDefinition(Medium.DISTRICT_COOLING, DEFAULT_DISTRICT_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ROOM_SENSOR =
    newSystemMeterDefinition(Medium.ROOM_SENSOR, DEFAULT_ROOM_SENSOR_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_ELECTRICITY =
    newSystemMeterDefinition(Medium.ELECTRICITY, DEFAULT_ELECTRICITY_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_GAS =
    newSystemMeterDefinition(Medium.GAS, DEFAULT_GAS_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_HOT_WATER =
    newSystemMeterDefinition(Medium.HOT_WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);
  public static final MeterDefinition DEFAULT_WATER =
    newSystemMeterDefinition(Medium.WATER, DEFAULT_WATER_DISPLAY_QUANTITIES);

  @Nullable
  public final Long id;
  @Nullable
  public final Organisation organisation;
  public final String name;
  public final Medium medium;
  public final boolean autoApply;
  @Builder.Default
  public final Set<DisplayQuantity> quantities = emptySet();

  public boolean isDefault() {
    return organisation == null;
  }

  @Override
  @Nullable
  public Long getId() {
    return id;
  }

  public boolean belongsTo(UUID organisationId) {
    return Optional.ofNullable(organisation)
      .map(org -> org.getId().equals(organisationId))
      .orElse(false);
  }

  private static MeterDefinition newSystemMeterDefinition(
    String mediumName,
    Set<DisplayQuantity> quantities
  ) {
    return builder()
      .medium(new Medium(null, mediumName))
      .name(mediumName.equals(Medium.UNKNOWN_MEDIUM)
        ? "Unknown"
        : "Default " + mediumName.toLowerCase())
      .autoApply(true)
      .quantities(quantities).build();
  }
}
