package com.elvaco.mvp.core.domainmodels;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

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
    assertThat(heatingMeter.getMedium()).isEqualTo("District heating meter");

    LogicalMeter coolingMeter = heatingMeter.withMeterDefinition(
      MeterDefinition.DISTRICT_COOLING_METER
    );
    assertThat(coolingMeter.getMedium()).isEqualTo("District cooling meter");
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
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    );

    LogicalMeter hotWaterMeter = heatingMeter.withMeterDefinition(MeterDefinition.HOT_WATER_METER);
    assertThat(hotWaterMeter.getQuantities()).containsOnly(
      Quantity.VOLUME,
      Quantity.FLOW,
      Quantity.TEMPERATURE
    );
  }

  @Test
  public void logicalMeterEquality() {
    Date now = new Date();
    UUID organisationId = randomUUID();
    UUID meterId = randomUUID();

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
      UUID.randomUUID(),
      UUID.randomUUID(),
      MeterDefinition.HOT_WATER_METER
    );

    assertThat(logicalMeter.getQuantity(Quantity.TEMPERATURE.name)).isNotEmpty();
    assertThat(logicalMeter.getQuantity("Bildäck")).isEmpty();
  }


  private LogicalMeter newLogicalMeter(
    UUID id,
    UUID organisationId,
    MeterDefinition meterDefinition
  ) {
    return new LogicalMeter(id, "an-external-id", organisationId, meterDefinition);
  }
}
