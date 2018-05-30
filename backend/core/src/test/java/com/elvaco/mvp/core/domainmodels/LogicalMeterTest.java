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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
  public void collectionPercentageNaNisNull() {
    LogicalMeter meter = newLogicalMeter(randomUUID(), randomUUID(), MeterDefinition.UNKNOWN_METER)
      .withCollectionPercentage(Double.NaN);

    assertThat(meter.collectionPercentage).isNull();
  }

  @Test
  public void collectionPercentageNullisNull() {
    LogicalMeter meter = newLogicalMeter(randomUUID(), randomUUID(), MeterDefinition.UNKNOWN_METER)
      .withCollectionPercentage(null);

    assertThat(meter.collectionPercentage).isNull();
  }

  @Test
  public void collectionPercentageIsSet() {
    LogicalMeter meter = newLogicalMeter(randomUUID(), randomUUID(), MeterDefinition.UNKNOWN_METER)
      .withCollectionPercentage(0.5);

    assertThat(meter.collectionPercentage).isEqualTo(0.5);
  }

  @Test
  public void collectionPercentageCannotBeLessThanZero() {
    LogicalMeter meter = newLogicalMeter(randomUUID(), randomUUID(), MeterDefinition.UNKNOWN_METER);

    assertThatThrownBy(() -> meter.withCollectionPercentage(-2.0))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(
        "Collection percentage must be >= 0 and <= 100, "
        + "but was: -2.0 for logical meter '" + meter.id + "'");
  }

  @Test
  public void collectionPercentageCannotBeGreaterThanOneHundred() {
    LogicalMeter meter = newLogicalMeter(randomUUID(), randomUUID(), MeterDefinition.UNKNOWN_METER);

    assertThatThrownBy(() -> meter.withCollectionPercentage(100.1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(
        "Collection percentage must be >= 0 and <= 100, "
        + "but was: 100.1 for logical meter '" + meter.id + "'");
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
      .measurementCount(0L)
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
      UNKNOWN_LOCATION,
      physicalMeterList
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
      0L,
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
      explicitStatus
    );
  }
}
