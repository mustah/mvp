package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.testing.fixture.DefaultTestFixture;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_COOLING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_HOT_WATER;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.UNKNOWN;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterTest extends DefaultTestFixture {

  @Test
  public void mediumFromMeterDefinition() {
    LogicalMeter heatingMeter = logicalMeter()
      .meterDefinition(DEFAULT_DISTRICT_HEATING)
      .build();
    assertThat(heatingMeter.getMedium()).isEqualTo(DEFAULT_DISTRICT_HEATING.medium);

    LogicalMeter coolingMeter = heatingMeter.toBuilder()
      .meterDefinition(DEFAULT_DISTRICT_COOLING)
      .build();
    assertThat(coolingMeter.getMedium()).isEqualTo(DEFAULT_DISTRICT_COOLING.medium);
  }

  @Test
  public void logicalMeterEquality() {
    UUID organisationId = randomUUID();
    UUID meterId = randomUUID();
    ZonedDateTime now = ZonedDateTime.now();

    LogicalMeter logicalMeter = logicalMeter()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(organisationId)
      .meterDefinition(DEFAULT_HOT_WATER)
      .created(now)
      .build();

    LogicalMeter otherLogicalMeter = logicalMeter()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(organisationId)
      .meterDefinition(DEFAULT_HOT_WATER)
      .created(now)
      .build();

    assertThat(logicalMeter).isEqualTo(otherLogicalMeter);
  }

  @Test
  public void getQuantity() {
    LogicalMeter logicalMeter = logicalMeter().meterDefinition(DEFAULT_HOT_WATER).build();

    assertThat(logicalMeter.getQuantity(Quantity.VOLUME.name)).isNotEmpty();
    assertThat(logicalMeter.getQuantity("Bildäck")).isEmpty();
  }

  @Test
  public void getManufacturerNoPhysicalMeter() {
    LogicalMeter logicalMeter = logicalMeter().meterDefinition(DEFAULT_HOT_WATER).build();

    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerUnknown() {
    LogicalMeter logicalMeter = logicalMeter()
      .physicalMeter(physicalMeter()
        .manufacturer(null)
        .build())
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("UNKNOWN");
  }

  @Test
  public void getManufacturerOnePhysicalMeter() {
    LogicalMeter logicalMeter = logicalMeter()
      .meterDefinition(DEFAULT_HOT_WATER)
      .physicalMeter(physicalMeter()
        .manufacturer("KAM")
        .build())
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("KAM");
  }

  @Test
  public void getManufacturerTwoPhysicalMeters() {
    ZonedDateTime now = ZonedDateTime.now();
    LogicalMeter logicalMeter = logicalMeter()
      .meterDefinition(DEFAULT_HOT_WATER)
      .physicalMeter(
        physicalMeter()
          .manufacturer("KAM")
          .activePeriod(PeriodRange.halfOpenFrom(now.minusDays(1), now))
          .build())
      .physicalMeter(
        physicalMeter()
          .manufacturer("ELV")
          .activePeriod(PeriodRange.halfOpenFrom(now, null))
          .build())
      .build();
    assertThat(logicalMeter.getManufacturer()).isEqualTo("ELV");
  }

  @Test
  public void currentStatus_unknownIfNoStatusAvailable() {
    var meter = logicalMeter()
      .physicalMeter(physicalMeter().build())
      .build();

    assertThat(meter.currentStatus()).isEqualTo(UNKNOWN);
  }

  @Test
  public void currentStatus_statusLogStatusIsUsedIfAvailable() {
    var physicalMeterId = randomUUID();

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
      .status(StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeterId, organisationId()))
        .status(ERROR)
        .build())
      .build();

    var meter = logicalMeter()
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(ERROR);
  }

  @Test
  public void currentStatus_latestStartedStatusLogUsedIfMultipleConcurrent() {
    var physicalMeterId = randomUUID();
    var primaryKey = new Pk(physicalMeterId, organisationId());

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
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
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(OK);
  }

  @Test
  public void currentStatus_stoppedStatusesAreNotConsidered() {
    var now = ZonedDateTime.now();
    var physicalMeterId = randomUUID();
    var primaryKey = new Pk(physicalMeterId, organisationId());

    var physicalMeter = physicalMeter()
      .id(physicalMeterId)
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
      .physicalMeter(physicalMeter)
      .build();

    assertThat(meter.currentStatus()).isEqualTo(ERROR);
  }
}
