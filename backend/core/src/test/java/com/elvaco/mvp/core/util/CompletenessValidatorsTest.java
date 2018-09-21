package com.elvaco.mvp.core.util;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import org.junit.Test;

import static com.elvaco.mvp.core.util.CompletenessValidators.gatewayValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.logicalMeterValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.physicalMeterValidator;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class CompletenessValidatorsTest {

  private static final Location KNOWN_LOCATION = new LocationBuilder()
    .country("Sweden")
    .city("City")
    .address("Address")
    .build();

  @Test
  public void logicalMeterValidatorLocationUnknownMissingMeterDefinition() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(randomUUID())
      .build();
    assertThat(logicalMeterValidator().isComplete(logicalMeter)).isFalse();
  }

  @Test
  public void logicalMeterValidatorLocationKnownMissingMeterDefinition() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(randomUUID())
      .location(KNOWN_LOCATION)
      .build();
    assertThat(logicalMeterValidator().isComplete(logicalMeter)).isFalse();
  }

  @Test
  public void logicalMeterValidatorLocationKnownMeterDefinitionKnown() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(randomUUID())
      .meterDefinition(MeterDefinition.DISTRICT_HEATING_METER)
      .location(KNOWN_LOCATION)
      .build();
    assertThat(logicalMeterValidator().isComplete(logicalMeter)).isTrue();
  }

  @Test
  public void logicalMeterValidatorLocationUnknownMeterDefinitionKnown() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(randomUUID())
      .meterDefinition(MeterDefinition.DISTRICT_HEATING_METER)
      .location(Location.UNKNOWN_LOCATION)
      .build();
    assertThat(logicalMeterValidator().isComplete(logicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorUnknownMedium() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.UNKNOWN_MEDIUM.medium)
      .manufacturer("ELV")
      .readIntervalMinutes(15)
      .build();
    assertThat(physicalMeterValidator().isComplete(physicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorComplete() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer("ELV")
      .readIntervalMinutes(15)
      .build();
    assertThat(physicalMeterValidator().isComplete(
      physicalMeter)).isTrue();
  }

  @Test
  public void physicalMeterValidatorUnknownManufacturer() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer(null)
      .readIntervalMinutes(15)
      .build();
    assertThat(physicalMeterValidator().isComplete(physicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorExplicitlyUnknownManufacturer() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer("UNKNOWN")
      .readIntervalMinutes(15)
      .build();
    assertThat(physicalMeterValidator().isComplete(physicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorZeroReportInterval() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer("ELV")
      .readIntervalMinutes(0)
      .build();
    assertThat(physicalMeterValidator().isComplete(physicalMeter)).isFalse();
  }

  @Test
  public void gatewayValidatorComplete() {
    Gateway gateway = Gateway.builder()
      .organisationId(randomUUID())
      .serial("1234")
      .productModel("CMi2110")
      .build();
    assertThat(gatewayValidator().isComplete(gateway)).isTrue();
  }

  @Test
  public void gatewayValidatorUnknownProductModel() {
    Gateway gateway = Gateway.builder()
      .organisationId(randomUUID())
      .serial("1234")
      .productModel("")
      .build();
    assertThat(gatewayValidator().isComplete(gateway)).isFalse();
  }
}
