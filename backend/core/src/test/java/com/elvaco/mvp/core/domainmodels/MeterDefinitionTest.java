package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionTest {

  @Test
  public void mapMediumToMeterDefinition() {
    assertThat(MeterDefinition.fromMedium(Medium.from("Hot water")))
      .isEqualTo(MeterDefinition.HOT_WATER_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("District heating")))
      .isEqualTo(MeterDefinition.DISTRICT_HEATING_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Heat, Return temp")))
      .isEqualTo(MeterDefinition.systemOwned(
        MeterDefinitionType.DISTRICT_HEATING_METER_TYPE,
        "Heat, Return temp",
        Medium.DISTRICT_HEATING.quantities()
      ));
  }

  @Test
  public void unknownMediumIsMappedToUnknownMeterDefinition() {
    assertThat(MeterDefinition.fromMedium(Medium.from("Some unsupported, unknown medium")))
      .isEqualTo(MeterDefinition.systemOwned(
        MeterDefinitionType.UNKNOWN_METER_TYPE,
        "Unknown medium",
        Medium.UNKNOWN_MEDIUM.quantities()
      ));
  }
}