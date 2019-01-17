package com.elvaco.mvp.core.util;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ROOM_SENSOR_METER;
import static com.elvaco.mvp.core.domainmodels.Quantity.QUANTITIES;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterHelperTest {

  private final LogicalMeterHelper logicalMeterHelper = new LogicalMeterHelper(name ->
    QUANTITIES.stream()
      .filter(quantity -> quantity.name.equals(name))
      .findAny()
      .orElse(null)
  );

  @Test
  public void groupByQuantity_twoMetersTwoQuantitiesForDifferentMediums() {
    assertThat(logicalMeterHelper.groupByQuantity(
      List.of(newMeter(DISTRICT_HEATING_METER), newMeter(HOT_WATER_METER)),
      Set.of(Quantity.ENERGY, Quantity.VOLUME)
    ))
      .hasEntrySatisfying(
        Quantity.VOLUME, physicalMeters -> assertThat(physicalMeters).hasSize(2)
      )
      .hasEntrySatisfying(
        Quantity.ENERGY, physicalMeters -> assertThat(physicalMeters).hasSize(1)
      );
  }

  @Test
  public void groupByQuantity_excludesQuantitiesWithoutMeters() {
    assertThat(logicalMeterHelper.groupByQuantity(
      List.of(newMeter(ROOM_SENSOR_METER), newMeter(HOT_WATER_METER)),
      Set.of(Quantity.ENERGY)
    )).isEmpty();
  }

  @Test
  public void groupByQuantity_looksUpUnits() {
    assertThat(logicalMeterHelper.groupByQuantity(
      List.of(newMeter(ROOM_SENSOR_METER)),
      Set.of(new Quantity("Relative humidity"))
    )).containsKeys(Quantity.HUMIDITY);
  }

  private LogicalMeter newMeter(MeterDefinition meterDefinition) {
    UUID meterId = randomUUID();
    UUID organisationId = randomUUID();
    return LogicalMeter.builder()
      .id(meterId)
      .externalId("meter-" + meterId)
      .organisationId(organisationId)
      .meterDefinition(meterDefinition)
      .physicalMeter(PhysicalMeter.builder()
        .organisationId(organisationId)
        .address("address")
        .externalId("external-id")
        .medium(meterDefinition.medium)
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build())
      .build();
  }
}
