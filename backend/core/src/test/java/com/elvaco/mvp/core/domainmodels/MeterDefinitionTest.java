package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionTest {

  @Test
  public void mapMediumToMeterDefinition() {
    assertThat(MeterDefinition.fromMedium(Medium.from("Hot water")))
      .isEqualTo(MeterDefinition.DEFAULT_HOT_WATER);

    assertThat(MeterDefinition.fromMedium(Medium.from("District heating")))
      .isEqualTo(MeterDefinition.DEFAULT_DISTRICT_HEATING);

    assertThat(MeterDefinition.fromMedium(Medium.from("District cooling")))
      .isEqualTo(MeterDefinition.DEFAULT_DISTRICT_COOLING);

    assertThat(MeterDefinition.fromMedium(Medium.from("Gas")))
      .isEqualTo(MeterDefinition.DEFAULT_GAS);

    assertThat(MeterDefinition.fromMedium(Medium.from("Water")))
      .isEqualTo(MeterDefinition.DEFAULT_WATER);

    assertThat(MeterDefinition.fromMedium(Medium.from("Electricity")))
      .isEqualTo(MeterDefinition.DEFAULT_ELECTRICITY);

    assertThat(MeterDefinition.fromMedium(Medium.from("Room sensor")))
      .isEqualTo(MeterDefinition.DEFAULT_ROOM_SENSOR);
  }

  @Test
  public void meteringMediumIsMappedToUnknown() {
    MeterDefinition unknownMeterDefinition = MeterDefinition.UNKNOWN;

    assertThat(MeterDefinition.fromMedium(Medium.from("Heat, Return temp")))
      .isEqualTo(unknownMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("Heat, Flow temp")))
      .isEqualTo(unknownMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatCoolingLoadMeter")))
      .isEqualTo(unknownMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatFlow Temp")))
      .isEqualTo(unknownMeterDefinition);

    assertThat(MeterDefinition.fromMedium(Medium.from("HeatReturn Temp")))
      .isEqualTo(unknownMeterDefinition);
  }

  @Test
  public void unknownMediumIsMappedToUnknownMeterDefinition() {
    assertThat(MeterDefinition.fromMedium(Medium.from("Some unsupported, unknown medium")))
      .isEqualTo(MeterDefinition.UNKNOWN);
  }
}
