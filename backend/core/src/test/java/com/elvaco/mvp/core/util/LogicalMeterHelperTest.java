package com.elvaco.mvp.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ROOM_SENSOR_METER;
import static com.elvaco.mvp.core.domainmodels.Quantity.QUANTITIES;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogicalMeterHelperTest {

  private final LogicalMeterHelper logicalMeterHelper = new LogicalMeterHelper(name ->
    QUANTITIES.stream()
      .filter(quantity -> quantity.name.equals(name))
      .findAny()
      .orElse(null)
  );

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_emptyParams() {
    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(emptyList(), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(singletonList(newMeter(
        DISTRICT_HEATING_METER
      )), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        emptyList(),
        singleton(Quantity.ENERGY)
      )).isEqualTo(emptyMap());
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_oneMeterOneQuantity() {
    LogicalMeter meter = newMeter(DISTRICT_HEATING_METER);
    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(Quantity.ENERGY, singletonList(meter.physicalMeters.get(0)))
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_twoMetersOneQuantity() {
    LogicalMeter meterOne = newMeter(DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(DISTRICT_HEATING_METER);
    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        asList(meterOne, meterTwo),
        singleton(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(
        Quantity.ENERGY,
        asList(meterOne.physicalMeters.get(0), meterTwo.physicalMeters.get(0))
      )
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_twoMetersTwoQuantities() {
    LogicalMeter meterOne = newMeter(DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(DISTRICT_HEATING_METER);

    Map<Quantity, List<PhysicalMeter>> expected = new HashMap<>();
    expected.put(
      Quantity.ENERGY,
      asList(meterOne.physicalMeters.get(0), meterTwo.physicalMeters.get(0))
    );
    expected.put(
      Quantity.VOLUME,
      asList(meterOne.physicalMeters.get(0), meterTwo.physicalMeters.get(0))
    );

    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.ENERGY, Quantity.VOLUME))
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_twoMetersTwoQuantitiesForDifferentMediums() {
    LogicalMeter meterOne = newMeter(DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(HOT_WATER_METER);

    assertThatThrownBy(() ->
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.TEMPERATURE, Quantity.VOLUME))
      )).isInstanceOf(
      InvalidQuantityForMeterType.class
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_customQuantityUnit() {
    Quantity volumeInSquareKilometers = new Quantity(
      null,
      Quantity.VOLUME.name,
      new QuantityPresentationInformation("km³", SeriesDisplayMode.CONSUMPTION),
      "m³"
    );
    LogicalMeter meter = newMeter(DISTRICT_HEATING_METER);

    Map<Quantity, List<PhysicalMeter>> expected = new HashMap<>();
    expected.put(
      volumeInSquareKilometers,
      singletonList(meter.physicalMeters.get(0))
    );

    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(volumeInSquareKilometers)
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_customQuantityWithoutUnit() {
    Quantity volumeWithNoUnit = new Quantity(Quantity.VOLUME.name);
    LogicalMeter meter = newMeter(DISTRICT_HEATING_METER);

    Map<Quantity, List<PhysicalMeter>> expected = new HashMap<>();
    expected.put(
      Quantity.VOLUME,
      singletonList(meter.physicalMeters.get(0))
    );

    assertThat(
      logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(volumeWithNoUnit)
      )).isEqualTo(
      expected
    );
  }

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
    return LogicalMeter.builder()
      .id(meterId)
      .externalId("meter-" + meterId)
      .organisationId(randomUUID())
      .meterDefinition(meterDefinition)
      .physicalMeter(PhysicalMeter.builder()
        .address("address")
        .externalId("external-id")
        .medium(meterDefinition.medium)
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build())
      .build();
  }
}
