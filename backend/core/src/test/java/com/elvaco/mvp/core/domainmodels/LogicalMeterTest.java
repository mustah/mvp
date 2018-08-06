package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterTest {

  @Test
  public void medium() {
    LogicalMeter heatingMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.DISTRICT_HEATING_METER
    );
    assertThat(heatingMeter.getMedium()).isEqualTo("District heating");

    LogicalMeter coolingMeter = heatingMeter.withMeterDefinition(
      MeterDefinition.DISTRICT_COOLING_METER
    );
    assertThat(coolingMeter.getMedium()).isEqualTo("District cooling");
  }

  @Test
  public void quantitiesDistrictHeatingMeter() {
    LogicalMeter heatingMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.DISTRICT_HEATING_METER
    );

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
    LogicalMeter waterMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.WATER_METER
    );

    assertThat(waterMeter.getQuantities()).containsOnly(
      Quantity.VOLUME
    );

    LogicalMeter hotWaterMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.HOT_WATER_METER
    );
    assertThat(hotWaterMeter.getQuantities()).containsOnly(
      Quantity.VOLUME
    );

    LogicalMeter coldWaterMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.COLD_WATER_METER
    );
    assertThat(coldWaterMeter.getQuantities()).containsOnly(
      Quantity.VOLUME
    );
  }

  @Test
  public void quantitiesElectricityMeter() {
    LogicalMeter meter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.ELECTRICITY_METER
    );

    assertThat(meter.getQuantities()).containsOnly(
      Quantity.ENERGY,
      Quantity.ENERGY_RETURN,
      Quantity.REACTIVE_ENERGY,
      Quantity.POWER
    );
  }

  @Test
  public void quantitiesRoomTempMeter() {
    LogicalMeter meter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.ROOM_TEMP_METER
    );

    assertThat(meter.getQuantities()).containsOnly(
      Quantity.TEMPERATURE,
      Quantity.HUMIDITY
    );
  }

  @Test
  public void logicalMeterEquality() {
    UUID organisationId = randomUUID();
    UUID meterId = randomUUID();
    ZonedDateTime now = ZonedDateTime.now();

    LogicalMeter logicalMeter = newLogicalMeter(
      meterId,
      organisationId,
      MeterDefinition.HOT_WATER_METER
    ).createdAt(now);

    LogicalMeter otherLogicalMeter = newLogicalMeter(
      meterId,
      organisationId,
      MeterDefinition.HOT_WATER_METER
    ).createdAt(now);

    assertThat(logicalMeter).isEqualTo(otherLogicalMeter);
  }

  @Test
  public void getQuantity() {
    LogicalMeter logicalMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.HOT_WATER_METER
    );

    assertThat(logicalMeter.getQuantity(Quantity.VOLUME.name)).isNotEmpty();
    assertThat(logicalMeter.getQuantity("Bildäck")).isEmpty();
  }

  @Test
  public void getManufacturerNoPhysicalMeter() {
    LogicalMeter logicalMeter = newLogicalMeter(
      randomUUID(),
      randomUUID(),
      MeterDefinition.HOT_WATER_METER
    );
    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerUnknown() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = newLogicalMeter(
      logicalMeterId,
      organisationId,
      singletonList(newPhysicalMeter(organisationId, logicalMeterId, null))
    );
    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerOnePhysicalMeter() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = newLogicalMeter(
      logicalMeterId,
      organisationId,
      singletonList(newPhysicalMeter(organisationId, logicalMeterId, "KAM"))
    );
    assertThat(logicalMeter.getManufacturer()).isEqualTo("KAM");
  }

  @Test
  public void getManufacturerTwoPhysicalMeters() {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = newLogicalMeter(
      logicalMeterId,
      organisationId,
      asList(
        newPhysicalMeter(organisationId, logicalMeterId, "KAM"),
        newPhysicalMeter(organisationId, logicalMeterId, "ELV")
      )
    );
    assertThat(logicalMeter.getManufacturer()).isEqualTo("ELV");
  }

  @Test
  public void currentStatus_explicitlySetStatusIsPreferred() {
    LogicalMeter meter = newLogicalMeterWithStatuses(new StatusLogEntry<>(
      randomUUID(),
      StatusType.ERROR,
      ZonedDateTime.now().minusDays(1)
    ), singletonList(
      new StatusLogEntry<>(
        randomUUID(),
        StatusType.OK,
        ZonedDateTime.now()
      )
    ));
    assertThat(meter.currentStatus()).isEqualTo(StatusType.ERROR);
  }

  @Test
  public void currentStatus_unknownIfNoStatusAvailable() {
    LogicalMeter meter = newLogicalMeterWithStatuses(
      null,
      emptyList()
    );

    assertThat(meter.currentStatus()).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void currentStatus_statusLogStatusIsUsedIfAvailable() {
    LogicalMeter meter = newLogicalMeterWithStatuses(
      null,
      singletonList(newStatusLog(StatusType.ERROR, ZonedDateTime.now()))
    );

    assertThat(meter.currentStatus()).isEqualTo(StatusType.ERROR);
  }

  @Test
  public void currentStatus_latestStartedStatusLogUsedIfMultipleConcurrent() {
    LogicalMeter meter = newLogicalMeterWithStatuses(
      null,
      asList(
        newStatusLog(StatusType.ERROR, ZonedDateTime.now()),
        newStatusLog(StatusType.OK, ZonedDateTime.now().plusHours(2)),
        newStatusLog(StatusType.WARNING, ZonedDateTime.now().minusDays(1))
      )
    );

    assertThat(meter.currentStatus()).isEqualTo(StatusType.OK);
  }

  @Test
  public void currentStatus_stoppedStatusesAreNotConsidered() {
    ZonedDateTime now = ZonedDateTime.now();
    LogicalMeter meter = newLogicalMeterWithStatuses(
      null,
      asList(
        newStatusLog(StatusType.ERROR, now),
        newStatusLog(StatusType.OK, now.plusHours(2), now.plusHours(3)),
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
    assertThat(collectionStats.actual).isEqualTo(0L);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(Double.NaN);
  }

  @Test
  public void getCollectionStatsOneExpectedAndNoneMissing() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(1L, 0L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.actual).isEqualTo(0.0);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(0);
  }

  @Test
  public void getCollectionStatsAllExpectedReadoutsAreMissing() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(1L, 1L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(1.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(100);
  }

  @Test
  public void getCollectionStats_SevenExpected_allCollected() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(7L, 7L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(7.0);
    assertThat(collectionStats.actual).isEqualTo(7.0);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(100);
  }

  @Test
  public void getCollectionStatsHaveSomeMissingReadouts() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(7L, 3L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(7.0);
    assertThat(collectionStats.actual).isEqualTo(3.0);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(42.857142857142854);
  }

  @Test
  public void getCollectionStats_noneExpectedOneReceived() {
    LogicalMeter meter = newLogicalMeterWithExpectedAndMissing(0L, 1L);

    CollectionStats collectionStats = meter.getCollectionStats();
    assertThat(collectionStats.expected).isEqualTo(0.0);
    assertThat(collectionStats.actual).isEqualTo(1.0);
    assertThat(collectionStats.getCollectionPercentage()).isEqualTo(Double.NaN);
  }

  private StatusLogEntry<UUID> newStatusLog(
    StatusType statusType,
    ZonedDateTime startTime,
    ZonedDateTime stopTime
  ) {
    return new StatusLogEntry<>(0L, randomUUID(), statusType, startTime, stopTime);
  }

  private StatusLogEntry<UUID> newStatusLog(StatusType statusType, ZonedDateTime startTime) {
    return new StatusLogEntry<>(randomUUID(), statusType, startTime);
  }

  private PhysicalMeter newPhysicalMeter(
    UUID organisationId,
    UUID logicalMeterId,
    String manufacturer
  ) {
    return PhysicalMeter.builder()
      .logicalMeterId(logicalMeterId)
      .organisation(new Organisation(
        organisationId,
        "an-organisation",
        "an-organisation",
        "an-organisation"
      ))
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer(manufacturer)
      .statuses(emptyList())
      .build();
  }

  private LogicalMeter newLogicalMeter(
    UUID id,
    UUID organisationId,
    List<PhysicalMeter> physicalMeterList
  ) {
    return new LogicalMeter(
      id,
      "an-external-id",
      organisationId,
      MeterDefinition.HOT_WATER_METER,
      ZonedDateTime.now(),
      physicalMeterList,
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION,
      null,
      null,
      null
    );
  }

  private LogicalMeter newLogicalMeter(
    UUID id,
    UUID organisationId,
    MeterDefinition meterDefinition
  ) {
    return new LogicalMeter(
      id,
      "an-external-id",
      organisationId,
      meterDefinition,
      UNKNOWN_LOCATION
    );
  }

  private LogicalMeter newLogicalMeterWithExpectedAndMissing(
    Long expectedMeasurementCount,
    Long missingMeasurementCount
  ) {
    return new LogicalMeter(
      randomUUID(),
      "an-external-id",
      randomUUID(),
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION,
      expectedMeasurementCount,
      missingMeasurementCount,
      null
    );
  }

  private LogicalMeter newLogicalMeterWithStatuses(
    @Nullable StatusLogEntry<UUID> explicitStatus,
    List<StatusLogEntry<UUID>> physicalMeterStatuses
  ) {
    UUID organisationId = randomUUID();
    UUID logicalMeterId = randomUUID();
    PhysicalMeter physicalMeter = new PhysicalMeter(
      randomUUID(),
      new Organisation(
        organisationId,
        "Organisation, Inc.",
        "organisation-inc",
        "Organisation, Inc."
      ),
      "250",
      "an-external-id",
      "Heat, Return temp.",
      "ELV",
      logicalMeterId,
      60L,
      physicalMeterStatuses
    );

    return new LogicalMeter(
      logicalMeterId,
      "an-external-id",
      randomUUID(),
      MeterDefinition.DISTRICT_HEATING_METER,
      ZonedDateTime.now(),
      singletonList(physicalMeter),
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION,
      null,
      null,
      explicitStatus
    );
  }
}
