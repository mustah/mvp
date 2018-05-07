package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletenessValidatorsTest {

  public static final Location KNOWN_LOCATION = new LocationBuilder().country("Sweden")
    .city("City")
    .address("Address")
    .build();

  @Test
  public void logicalMeterValidatorLocationUnknownMissingMeterDefinition() {
    LogicalMeter logicalMeter = newLogicalMeter(
      Location.UNKNOWN_LOCATION,
      MeterDefinition.UNKNOWN_METER
    );
    assertThat(CompletenessValidators.logicalMeter().isComplete(
      logicalMeter)).isFalse();
  }

  @Test
  public void logicalMeterValidatorLocationKnownMissingMeterDefinition() {
    LogicalMeter logicalMeter = newLogicalMeter(
      KNOWN_LOCATION,
      MeterDefinition.UNKNOWN_METER
    );
    assertThat(CompletenessValidators.logicalMeter().isComplete(
      logicalMeter)).isFalse();
  }

  @Test
  public void logicalMeterValidatorLocationKnownMeterDefinitionKnown() {
    LogicalMeter logicalMeter = newLogicalMeter(
      KNOWN_LOCATION,
      MeterDefinition.DISTRICT_HEATING_METER
    );
    assertThat(CompletenessValidators.logicalMeter().isComplete(
      logicalMeter)).isTrue();
  }

  @Test
  public void logicalMeterValidatorLocationUnknownMeterDefinitionKnown() {
    LogicalMeter logicalMeter = newLogicalMeter(
      Location.UNKNOWN_LOCATION,
      MeterDefinition.DISTRICT_HEATING_METER
    );
    assertThat(CompletenessValidators.logicalMeter().isComplete(
      logicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorUnknownMedium() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.UNKNOWN_MEDIUM.medium)
      .manufacturer("ELV")
      .readIntervalMinutes(15)
      .build();
    assertThat(CompletenessValidators.physicalMeter().isComplete(
      physicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorComplete() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer("ELV")
      .readIntervalMinutes(15)
      .build();
    assertThat(CompletenessValidators.physicalMeter().isComplete(
      physicalMeter)).isTrue();
  }

  @Test
  public void physicalMeterValidatorUnknownManufacturer() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer(null)
      .readIntervalMinutes(15)
      .build();
    assertThat(CompletenessValidators.physicalMeter().isComplete(
      physicalMeter)).isFalse();
  }

  @Test
  public void physicalMeterValidatorExplicitlyUnknownManufacturer() {
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .medium(Medium.DISTRICT_HEATING.medium)
      .manufacturer("UNKNOWN")
      .readIntervalMinutes(15)
      .build();
    assertThat(CompletenessValidators.physicalMeter().isComplete(
      physicalMeter)).isFalse();
  }

  @Test
  public void gatewayValidatorComplete() {
    Gateway gateway = new Gateway(UUID.randomUUID(), UUID.randomUUID(), "1234", "CMi2110");
    assertThat(CompletenessValidators.gateway().isComplete(
      gateway)).isTrue();
  }

  @Test
  public void gatewayValidatorUnknownProductModel() {
    Gateway gateway = new Gateway(UUID.randomUUID(), UUID.randomUUID(), "1234", "");
    assertThat(CompletenessValidators.gateway().isComplete(
      gateway)).isFalse();
  }

  private LogicalMeter newLogicalMeter(Location location, MeterDefinition meterDefinition) {
    return new LogicalMeter(
      UUID.randomUUID(),
      "external-id",
      UUID.randomUUID(),
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now()
    )
      .withLocation(location)
      .withMeterDefinition(meterDefinition);
  }
}
