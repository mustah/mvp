package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_COOLING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ELECTRICITY_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ROOM_SENSOR_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.WATER_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.SECRET_SERVICE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterTest {

  @Test
  public void medium() {
    LogicalMeter heatingMeter = logicalMeterBuilder()
      .meterDefinition(DISTRICT_HEATING_METER)
      .build();
    assertThat(heatingMeter.getMedium()).isEqualTo("District heating");

    LogicalMeter coolingMeter = heatingMeter.toBuilder()
      .meterDefinition(DISTRICT_COOLING_METER)
      .build();
    assertThat(coolingMeter.getMedium()).isEqualTo("District cooling");
  }

  @Test
  public void quantitiesDistrictHeatingMeter() {
    LogicalMeter heatingMeter = logicalMeterBuilder()
      .meterDefinition(DISTRICT_HEATING_METER)
      .build();

    assertThat(heatingMeter.getQuantities()).containsOnly(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.VOLUME_FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    );
  }

  @Test
  public void quantitiesWaterMeters() {
    LogicalMeter waterMeter = logicalMeterBuilder()
      .meterDefinition(WATER_METER)
      .build();

    assertThat(waterMeter.getQuantities()).containsOnly(Quantity.VOLUME);
  }

  @Test
  public void quantitiesElectricityMeter() {
    LogicalMeter meter = logicalMeterBuilder()
      .meterDefinition(ELECTRICITY_METER)
      .build();

    assertThat(meter.getQuantities()).containsOnly(
      Quantity.ENERGY,
      Quantity.ENERGY_RETURN,
      Quantity.REACTIVE_ENERGY,
      Quantity.POWER
    );
  }

  @Test
  public void quantitiesRoomTempMeter() {
    LogicalMeter meter = logicalMeterBuilder()
      .meterDefinition(ROOM_SENSOR_METER)
      .build();

    assertThat(meter.getQuantities()).containsOnly(
      Quantity.EXTERNAL_TEMPERATURE,
      Quantity.HUMIDITY
    );
  }

  @Test
  public void logicalMeterEquality() {
    UUID organisationId = randomUUID();
    UUID meterId = randomUUID();
    ZonedDateTime now = ZonedDateTime.now();

    LogicalMeter logicalMeter = logicalMeterBuilder()
      .id(meterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .created(now)
      .build();

    LogicalMeter otherLogicalMeter = logicalMeterBuilder()
      .id(meterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .created(now)
      .build();

    assertThat(logicalMeter).isEqualTo(otherLogicalMeter);
  }

  @Test
  public void getQuantity() {
    LogicalMeter logicalMeter = logicalMeterBuilder().meterDefinition(HOT_WATER_METER).build();

    assertThat(logicalMeter.getQuantity(Quantity.VOLUME.name)).isNotEmpty();
    assertThat(logicalMeter.getQuantity("Bildäck")).isEmpty();
  }

  @Test
  public void getManufacturerNoPhysicalMeter() {
    LogicalMeter logicalMeter = logicalMeterBuilder().meterDefinition(HOT_WATER_METER).build();

    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerUnknown() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = logicalMeterBuilder()
      .id(logicalMeterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .physicalMeter(newPhysicalMeter(logicalMeterId, null))
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerOnePhysicalMeter() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = logicalMeterBuilder()
      .id(logicalMeterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .physicalMeter(newPhysicalMeter(logicalMeterId, "KAM"))
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("KAM");
  }

  @Test
  public void getManufacturerTwoPhysicalMeters() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = logicalMeterBuilder()
      .id(logicalMeterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .physicalMeter(newPhysicalMeter(logicalMeterId, "KAM"))
      .physicalMeter(newPhysicalMeter(logicalMeterId, "ELV"))
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("ELV");
  }

  @Test
  public void currentStatus_unknownIfNoStatusAvailable() {
    LogicalMeter meter = newLogicalMeterWithStatuses(emptyList());

    assertThat(meter.currentStatus()).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void currentStatus_statusLogStatusIsUsedIfAvailable() {
    LogicalMeter meter = newLogicalMeterWithStatuses(
      singletonList(newStatusLog(StatusType.ERROR, ZonedDateTime.now()))
    );

    assertThat(meter.currentStatus()).isEqualTo(StatusType.ERROR);
  }

  @Test
  public void currentStatus_latestStartedStatusLogUsedIfMultipleConcurrent() {
    LogicalMeter meter = newLogicalMeterWithStatuses(
      asList(
        newStatusLog(StatusType.ERROR, ZonedDateTime.now()),
        newStatusLog(OK, ZonedDateTime.now().plusHours(2)),
        newStatusLog(StatusType.WARNING, ZonedDateTime.now().minusDays(1))
      )
    );

    assertThat(meter.currentStatus()).isEqualTo(OK);
  }

  @Test
  public void currentStatus_stoppedStatusesAreNotConsidered() {
    ZonedDateTime now = ZonedDateTime.now();
    LogicalMeter meter = newLogicalMeterWithStatuses(
      asList(
        newStatusLog(StatusType.ERROR, now),
        newOkStatusLog(now.plusHours(2), now.plusHours(3)),
        newStatusLog(StatusType.WARNING, now.minusDays(1))
      )
    );

    assertThat(meter.currentStatus()).isEqualTo(StatusType.ERROR);
  }

  @Test
  public void getCollectionStats_noneExpected() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(null, null);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(0L);
    assertThat(collectionStats.missing).isEqualTo(0L);
    assertThat(collectionStats.collectionPercentage).isEqualTo(Double.NaN);
  }

  @Test
  public void getCollectionStatsOneExpectedAndNoneMissing() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(1L, 0L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.missing).isEqualTo(0.0);
    assertThat(collectionStats.collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void getCollectionStatsAllExpectedReadoutsAreMissing() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(1L, 1L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.missing).isEqualTo(1.0);
    assertThat(collectionStats.collectionPercentage).isEqualTo(0);
  }

  @Test
  public void getCollectionStats_SevenExpected_allCollected() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(7L, 7L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(7.0);
    assertThat(collectionStats.missing).isEqualTo(7.0);
    assertThat(collectionStats.collectionPercentage).isEqualTo(0);
  }

  @Test
  public void getCollectionStatsHaveSomeMissingReadouts() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(7L, 3L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(7.0);
    assertThat(collectionStats.missing).isEqualTo(3.0);
    assertThat(collectionStats.collectionPercentage).isEqualTo(57.14285714285714);
  }

  @Test
  public void getCollectionStats_noneExpectedOneReceived() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(0L, 1L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.collectionPercentage).isEqualTo(Double.NaN);
    assertThat(collectionStats.expected).isEqualTo(0.0);
    assertThat(collectionStats.missing).isEqualTo(1.0);
  }

  private static LogicalMeter.LogicalMeterBuilder logicalMeterBuilder() {
    return LogicalMeter.builder()
      .organisationId(randomUUID())
      .externalId("an-external-id");
  }

  private static LogicalMeter newLogicalMeterWithExpectedAndMissing(
    Long expectedMeasurementCount,
    Long missingMeasurementCount
  ) {
    return logicalMeterBuilder()
      .expectedMeasurementCount(expectedMeasurementCount)
      .missingMeasurementCount(missingMeasurementCount)
      .build();
  }

  private static LogicalMeter newLogicalMeterWithStatuses(
    List<StatusLogEntry<UUID>> physicalMeterStatuses
  ) {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisationId(OTHER_ORGANISATION.id)
      .address("250")
      .externalId("an-external-id")
      .medium("Heat, Return temp.")
      .manufacturer("ELV")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(60)
      .statuses(physicalMeterStatuses)
      .build();

    return logicalMeterBuilder()
      .id(logicalMeterId)
      .organisationId(organisationId)
      .meterDefinition(DISTRICT_HEATING_METER)
      .physicalMeter(physicalMeter)
      .build();
  }

  private static StatusLogEntry<UUID> newOkStatusLog(
    ZonedDateTime startTime,
    ZonedDateTime stopTime
  ) {
    return StatusLogEntry.<UUID>builder()
      .id(0L)
      .entityId(randomUUID())
      .status(OK)
      .start(startTime)
      .stop(stopTime)
      .build();
  }

  private static StatusLogEntry<UUID> newStatusLog(StatusType statusType, ZonedDateTime startTime) {
    return StatusLogEntry.<UUID>builder()
      .entityId(randomUUID())
      .status(statusType)
      .start(startTime)
      .build();
  }

  private static PhysicalMeter newPhysicalMeter(UUID logicalMeterId, String manufacturer) {
    return PhysicalMeter.builder()
      .logicalMeterId(logicalMeterId)
      .organisationId(SECRET_SERVICE.id)
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer(manufacturer)
      .build();
  }
}
