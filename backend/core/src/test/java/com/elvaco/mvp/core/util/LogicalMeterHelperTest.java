package com.elvaco.mvp.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterHelperTest {

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_emptyParams() {
    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(emptyList(), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(singletonList(newMeter(
        randomUUID(),
        DISTRICT_HEATING_METER
      )), emptySet()))
      .isEqualTo(emptyMap());

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        emptyList(),
        singleton(Quantity.ENERGY)
      )).isEqualTo(singletonMap(Quantity.ENERGY, emptyList()));
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_oneMeterOneQuantity() {
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singleton(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(Quantity.ENERGY, singletonList(meter.physicalMeters.get(0).id))
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_twoMetersOneQuantity() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        singleton(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(
        Quantity.ENERGY,
        asList(meterOne.physicalMeters.get(0).id, meterTwo.physicalMeters.get(0).id)
      )
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_twoMetersTwoQuantities() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), DISTRICT_HEATING_METER);

    Map<Quantity, List<UUID>> expected = new HashMap<>();
    expected.put(
      Quantity.ENERGY,
      asList(meterOne.physicalMeters.get(0).id, meterTwo.physicalMeters.get(0).id)
    );
    expected.put(
      Quantity.VOLUME,
      asList(meterOne.physicalMeters.get(0).id, meterTwo.physicalMeters.get(0).id)
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.ENERGY, Quantity.VOLUME))
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_twoMetersTwoQuantitiesForDifferentMediums() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), HOT_WATER_METER);

    Map<Quantity, List<UUID>> expected = new HashMap<>();
    expected.put(
      Quantity.TEMPERATURE,
      singletonList(meterTwo.physicalMeters.get(0).id)
    );
    expected.put(
      Quantity.VOLUME,
      asList(meterOne.physicalMeters.get(0).id, meterTwo.physicalMeters.get(0).id)
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        new HashSet<>(asList(Quantity.TEMPERATURE, Quantity.VOLUME))
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_customQuantityUnit() {
    Quantity volumeInSquareKilometers = new Quantity(Quantity.VOLUME.name, "km³");
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);

    Map<Quantity, List<UUID>> expected = new HashMap<>();
    expected.put(
      volumeInSquareKilometers,
      singletonList(meter.physicalMeters.get(0).id)
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singleton(volumeInSquareKilometers)
      )).isEqualTo(
      expected
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_customQuantityWithoutUnit() {
    Quantity volumeWithNoUnit = new Quantity(Quantity.VOLUME.name);
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);

    Map<Quantity, List<UUID>> expected = new HashMap<>();
    expected.put(
      Quantity.VOLUME,
      singletonList(meter.physicalMeters.get(0).id)
    );

    assertThat(
      mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singleton(volumeWithNoUnit)
      )).isEqualTo(
      expected
    );
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
      singletonList(
        new PhysicalMeter(
          randomUUID(),
          null,
          "address",
          "external-id",
          meterDefinition.medium,
          "manufacturer",
          meterId,
          15,
          0L
        )
      )
    );
  }
}
