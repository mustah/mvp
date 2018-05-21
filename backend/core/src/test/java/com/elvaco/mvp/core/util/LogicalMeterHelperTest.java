package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.calculateExpectedReadOuts;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeters;
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

  @Test
  public void calculateExpectedReadoutsHourly() {
    assertThat(calculateExpectedReadOuts(
      60,
      ZonedDateTime.parse("2018-01-01T00:00:00Z"),
      ZonedDateTime.parse("2018-01-02T00:00:00Z")
    )).isEqualTo(24);
  }

  @Test
  public void calculateExpectedReadoutsFifteenMinutes() {
    assertThat(calculateExpectedReadOuts(
      15,
      ZonedDateTime.parse("2018-01-01T00:00:00Z"),
      ZonedDateTime.parse("2018-01-02T00:00:00Z")
    )).isEqualTo(96);
  }

  @Test
  public void calculateExpectedReadoutsForZeroInterval() {
    assertThat(calculateExpectedReadOuts(
      0,
      ZonedDateTime.parse("2018-01-01T00:00:00Z"),
      ZonedDateTime.parse("2018-01-02T00:00:00Z")
    )).isEqualTo(0);
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_emptyParams() {
    assertThat(
      mapMeterQuantitiesToPhysicalMeters(emptyList(), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      mapMeterQuantitiesToPhysicalMeters(singletonList(newMeter(
        randomUUID(),
        DISTRICT_HEATING_METER
      )), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      mapMeterQuantitiesToPhysicalMeters(
        emptyList(),
        singleton(Quantity.ENERGY)
      )).isEqualTo(emptyMap());
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_oneMeterOneQuantity() {
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(Quantity.ENERGY, singletonList(meter.physicalMeters.get(0)))
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_twoMetersOneQuantity() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      mapMeterQuantitiesToPhysicalMeters(
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
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), DISTRICT_HEATING_METER);

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
      mapMeterQuantitiesToPhysicalMeters(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.ENERGY, Quantity.VOLUME))
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_twoMetersTwoQuantitiesForDifferentMediums() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), HOT_WATER_METER);

    assertThatThrownBy(() ->
      mapMeterQuantitiesToPhysicalMeters(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.TEMPERATURE, Quantity.VOLUME))
      )).isInstanceOf(
      InvalidQuantityForMeterType.class
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_customQuantityUnit() {
    Quantity volumeInSquareKilometers = new Quantity(
      Quantity.VOLUME.name,
      new QuantityPresentationInformation("km³", SeriesDisplayMode.CONSUMPTION)
    );
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);

    Map<Quantity, List<PhysicalMeter>> expected = new HashMap<>();
    expected.put(
      volumeInSquareKilometers,
      singletonList(meter.physicalMeters.get(0))
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(volumeInSquareKilometers)
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeters_customQuantityWithoutUnit() {
    Quantity volumeWithNoUnit = new Quantity(Quantity.VOLUME.name);
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);

    Map<Quantity, List<PhysicalMeter>> expected = new HashMap<>();
    expected.put(
      Quantity.VOLUME,
      singletonList(meter.physicalMeters.get(0))
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeters(
        singletonList(meter),
        singleton(volumeWithNoUnit)
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void getCollectionPercent_zeroExpectedQuantities() {
    ZonedDateTime now = ZonedDateTime.now();
    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      emptyList(),
      now,
      now,
      0
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(Double.NaN);
    assertThat(collectionStats.actual).isEqualTo(0.0);
    assertThat(collectionStats.expected).isEqualTo(0.0);
  }

  @Test
  public void getCollectionPercent_zeroMeters() {
    ZonedDateTime now = ZonedDateTime.now();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      emptyList(),
      now,
      now,
      1
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(Double.NaN);
    assertThat(collectionStats.actual).isEqualTo(0.0);
    assertThat(collectionStats.expected).isEqualTo(0.0);
  }

  @Test
  public void getCollectionPercent_oneQuantityOneMeter_zeroCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder().readIntervalMinutes(1).build();
    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(1),
      1
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(0.0);
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.actual).isEqualTo(0.0);
  }

  @Test
  public void getCollectionPercent_oneQuantityOneMeter_allCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(1),
      1
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(100.0);
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
  }

  @Test
  public void getCollectionPercent_oneQuantityOneMeter_halfCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(2),
      1
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(50.0);
    assertThat(collectionStats.expected).isEqualTo(2.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
  }

  @Test
  public void getCollectionPercent_oneQuantityTwoMeters_allCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter firstMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    PhysicalMeter secondMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      asList(firstMeter, secondMeter),
      now,
      now.plusMinutes(1),
      1
    );
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(100.0);
    assertThat(collectionStats.expected).isEqualTo(2.0);
    assertThat(collectionStats.actual).isEqualTo(2.0);
  }

  @Test
  public void getCollectionPercent_oneQuantityTwoMeters_halfCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter firstMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    PhysicalMeter secondMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(0L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      asList(firstMeter, secondMeter),
      now,
      now.plusMinutes(1),
      1
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(50.0);
    assertThat(collectionStats.expected).isEqualTo(2.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
  }

  @Test
  public void getCollectionPercent_twoQuantitiesOneMeter_allCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(2L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(1),
      2
    );

    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(100.0);
    assertThat(collectionStats.actual).isEqualTo(2.0);
    assertThat(collectionStats.expected).isEqualTo(2.0);
  }

  @Test
  public void getCollectionPercent_twoQuantitiesOneMeter_halfCollected() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .readIntervalMinutes(1)
      .measurementCount(1L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(1),
      2
    );
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(50.0);
    assertThat(collectionStats.expected).isEqualTo(2.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
  }

  @Test
  public void getCollectionPercent_noneExpectedOneReceived() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .readIntervalMinutes(0)
      .measurementCount(1L)
      .build();

    CollectionStats collectionStats = LogicalMeterHelper.getCollectionPercent(
      singletonList(physicalMeter),
      now,
      now.plusMinutes(1),
      1
    );
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(Double.NaN);
    assertThat(collectionStats.expected).isEqualTo(0.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
  }

  private LogicalMeter newMeter(
    UUID meterId,
    MeterDefinition meterDefinition
  ) {
    UUID organisationId = randomUUID();
    return new LogicalMeter(
      meterId,
      "meter-" + meterId,
      organisationId,
      meterDefinition,
      UNKNOWN_LOCATION,
      singletonList(PhysicalMeter.builder()
        .address("address")
        .externalId("external-id")
        .medium(meterDefinition.medium)
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .measurementCount(0L)
        .build())
    );
  }
}
