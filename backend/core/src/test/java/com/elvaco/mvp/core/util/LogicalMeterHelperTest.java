package com.elvaco.mvp.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterHelperTest {

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_emptyParams() {
    assertThat(
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(emptyList(), emptyList()))
      .isEqualTo(emptyMap());

    assertThat(
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(singletonList(newMeter(
        randomUUID(),
        DISTRICT_HEATING_METER
      )), emptyList()))
      .isEqualTo(emptyMap());

    assertThat(
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        emptyList(),
        singletonList(Quantity.ENERGY)
      )).isEqualTo(singletonMap(Quantity.ENERGY, emptyList()));
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_oneMeterOneQuantity() {
    LogicalMeter meter = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singletonList(Quantity.ENERGY)
      )).isEqualTo(
      singletonMap(Quantity.ENERGY, singletonList(meter.physicalMeters.get(0).id))
    );
  }

  @Test
  public void mapMeterQuantitiesToPhysicalMeterUuids_twoMetersOneQuantity() {
    LogicalMeter meterOne = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    LogicalMeter meterTwo = newMeter(randomUUID(), DISTRICT_HEATING_METER);
    assertThat(
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        singletonList(Quantity.ENERGY)
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
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        asList(Quantity.ENERGY, Quantity.VOLUME)
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
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        asList(meterOne, meterTwo),
        asList(Quantity.TEMPERATURE, Quantity.VOLUME)
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
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singletonList(volumeInSquareKilometers)
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
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        singletonList(meter),
        singletonList(volumeWithNoUnit)
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
