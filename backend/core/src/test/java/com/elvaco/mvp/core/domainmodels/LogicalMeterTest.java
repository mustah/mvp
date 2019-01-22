package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_COOLING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ELECTRICITY_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.ROOM_SENSOR_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.WATER_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.UNKNOWN;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.SECRET_SERVICE;
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
    ZonedDateTime now = ZonedDateTime.now();
    UUID logicalMeterId = randomUUID();
    LogicalMeter logicalMeter = logicalMeterBuilder()
      .id(logicalMeterId)
      .organisationId(organisationId)
      .meterDefinition(HOT_WATER_METER)
      .physicalMeter(
        newPhysicalMeter(logicalMeterId, "KAM", PeriodRange.halfOpenFrom(now.minusDays(1), now)
        ))
      .physicalMeter(
        newPhysicalMeter(logicalMeterId, "ELV", PeriodRange.halfOpenFrom(now, null)
        ))
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("ELV");
  }

  @Test
  public void currentStatus_unknownIfNoStatusAvailable() {
    var logicalMeterId = randomUUID();

    var physicalMeter = physicalMeter()
      .logicalMeterId(logicalMeterId)
      .build();

    var meter = logicalMeter()
      .id(logicalMeterId)
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(UNKNOWN);
  }

  @Test
  public void currentStatus_statusLogStatusIsUsedIfAvailable() {
    var logicalMeterId = randomUUID();
    var physicalMeterId = randomUUID();

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeterId)
      .status(StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeterId, OTHER_ORGANISATION.id))
        .status(ERROR)
        .build())
      .build();

    var meter = logicalMeter()
      .id(logicalMeterId)
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(ERROR);
  }

  @Test
  public void currentStatus_latestStartedStatusLogUsedIfMultipleConcurrent() {
    var logicalMeterId = randomUUID();
    var physicalMeterId = randomUUID();
    var primaryKey = new Pk(physicalMeterId, OTHER_ORGANISATION.id);

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeterId)
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(ERROR)
        .build())
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(OK)
        .start(ZonedDateTime.now().plusHours(2))
        .build())
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(WARNING)
        .start(ZonedDateTime.now().minusDays(1))
        .build())
      .build();

    var meter = logicalMeter()
      .id(logicalMeterId)
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(OK);
  }

  @Test
  public void currentStatus_stoppedStatusesAreNotConsidered() {
    var now = ZonedDateTime.now();
    var logicalMeterId = randomUUID();
    var physicalMeterId = randomUUID();
    var primaryKey = new Pk(physicalMeterId, OTHER_ORGANISATION.id);

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeterId)
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .start(now)
        .status(ERROR)
        .build())
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(OK)
        .start(now.plusHours(2))
        .stop(now.plusHours(3))
        .build())
      .status(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(WARNING)
        .start(now.minusDays(1))
        .build())
      .build();

    var meter = logicalMeter()
      .id(logicalMeterId)
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(ERROR);
  }

  private static LogicalMeter.LogicalMeterBuilder logicalMeterBuilder() {
    return LogicalMeter.builder()
      .organisationId(randomUUID())
      .externalId("an-external-id");
  }

  private static LogicalMeter.LogicalMeterBuilder logicalMeter() {
    return LogicalMeter.builder()
      .organisationId(OTHER_ORGANISATION.id)
      .meterDefinition(DISTRICT_HEATING_METER);
  }

  private static PhysicalMeter newPhysicalMeter(
    UUID logicalMeterId,
    @Nullable String manufacturer,
    PeriodRange activePeriod
  ) {
    return PhysicalMeter.builder()
      .logicalMeterId(logicalMeterId)
      .organisationId(SECRET_SERVICE.id)
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer(manufacturer)
      .activePeriod(activePeriod)
      .build();
  }

  private static PhysicalMeter newPhysicalMeter(
    UUID logicalMeterId,
    @Nullable String manufacturer
  ) {
    return PhysicalMeter.builder()
      .logicalMeterId(logicalMeterId)
      .organisationId(SECRET_SERVICE.id)
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer(manufacturer)
      .activePeriod(PeriodRange.unbounded())
      .build();
  }

  private static PhysicalMeter.PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisationId(OTHER_ORGANISATION.id)
      .address("250")
      .externalId("an-external-id")
      .medium("Heat, Return temp.")
      .manufacturer("ELV")
      .activePeriod(PeriodRange.unbounded())
      .readIntervalMinutes(60);
  }
}
