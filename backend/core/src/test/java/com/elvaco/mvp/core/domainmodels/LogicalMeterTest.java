package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterTest {

  @Test
  public void testMedium() {
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
  public void testQuantities() {
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

    LogicalMeter hotWaterMeter = heatingMeter.withMeterDefinition(MeterDefinition.HOT_WATER_METER);
    assertThat(hotWaterMeter.getQuantities()).containsOnly(
      Quantity.VOLUME,
      Quantity.VOLUME_FLOW,
      Quantity.TEMPERATURE
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

    assertThat(logicalMeter.getQuantity(Quantity.TEMPERATURE.name)).isNotEmpty();
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
      Collections.singletonList(newPhysicalMeter(organisationId, logicalMeterId, null))
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
      Collections.singletonList(newPhysicalMeter(organisationId, logicalMeterId, "KAM"))
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
}
