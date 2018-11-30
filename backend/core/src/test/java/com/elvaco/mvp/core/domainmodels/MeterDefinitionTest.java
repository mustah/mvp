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

    assertThat(MeterDefinition.fromMedium(Medium.from("District cooling")))
      .isEqualTo(MeterDefinition.DISTRICT_COOLING_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Gas")))
      .isEqualTo(MeterDefinition.GAS_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Water")))
      .isEqualTo(MeterDefinition.WATER_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Electricity")))
      .isEqualTo(MeterDefinition.ELECTRICITY_METER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Room sensor")))
      .isEqualTo(MeterDefinition.ROOM_SENSOR_METER);
  }

  @Test
  public void meteringMediumIsMappedToUnknown() {
    MeterDefinition unkonwMeterDefinition = MeterDefinition.systemOwned(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "Unknown medium",
      Medium.UNKNOWN_MEDIUM.quantities()
    );

    assertThat(MeterDefinition.fromMedium(Medium.from("Heat, Return temp")))
      .isEqualTo(unkonwMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("Heat, Flow temp")))
      .isEqualTo(unkonwMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatCoolingLoadMeter")))
      .isEqualTo(unkonwMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatFlow Temp")))
      .isEqualTo(unkonwMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatReturn Temp")))
      .isEqualTo(unkonwMeterDefinition);
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
