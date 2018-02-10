package com.elvaco.mvp.core.domainmodels;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterTest {
  @Test
  public void testMedium() {
    LogicalMeter logicalMeter = new LogicalMeter(MeterDefinition.DISTRICT_HEATING_METER);
    assertThat(logicalMeter.getMedium()).isEqualTo("District heating meter");

    logicalMeter.setMeterDefinition(MeterDefinition.DISTRICT_COOLING_METER);
    assertThat(logicalMeter.getMedium()).isEqualTo("District cooling meter");
  }

  @Test
  public void testQuantities() {
    LogicalMeter logicalMeter = new LogicalMeter(MeterDefinition.DISTRICT_HEATING_METER);
    assertThat(logicalMeter.getQuantities()).hasSameElementsAs(Arrays.asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.POWER,
      Quantity.FLOW,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE,
      Quantity.DIFFERENCE_TEMPERATURE
    ));

    logicalMeter.setMeterDefinition(MeterDefinition.HOT_WATER_METER);
    assertThat(logicalMeter.getQuantities()).hasSameElementsAs(Arrays.asList(
      Quantity.VOLUME,
      Quantity.FLOW,
      Quantity.TEMPERATURE
    ));
  }
}
