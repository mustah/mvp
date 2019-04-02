package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.Medium;

import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mapToEvoMedium;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMedium;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageMapperTest {

  @Test
  public void unknownMediumIsMappedFromEmptyValueSet() {
    assertThat(resolveMedium(emptyList())).isEqualTo(Medium.UNKNOWN_MEDIUM);
  }

  @Test
  public void unknownMediumIsMappedFromUnknownValueSet() {
    List<ValueDto> values = singletonList(
      new ValueDto(LocalDateTime.now(), 0.0, "MW", "UnknownQuantity")
    );

    assertThat(resolveMedium(values)).isEqualTo(Medium.UNKNOWN_MEDIUM);
  }

  @Test
  public void bothUnknownAndKnownQuantitiesAreMappedToUnknownMedium() {
    List<ValueDto> values = asList(
      newValueDto("Volume"),
      newValueDto("Bluahe")
    );

    assertThat(resolveMedium(values)).isEqualTo(Medium.UNKNOWN_MEDIUM);
  }

  @Test
  public void mapColdWaterMediumAsWater() {
    Medium waterMedium = new Medium(0L, Medium.WATER);
    MediumProvider mediumProvider = providerOf(Map.of(Medium.WATER, waterMedium));

    assertThat(mapToEvoMedium(mediumProvider, "Cold water")).isEqualTo(waterMedium);
  }

  @Test
  public void mapUnknownMeteringMediumToUnknownMedium() {
    Medium unknownMedium = new Medium(0L, Medium.UNKNOWN_MEDIUM);
    MediumProvider mediumProvider = providerOf(Map.of(Medium.UNKNOWN_MEDIUM, unknownMedium));
    assertThat(mapToEvoMedium(mediumProvider, "Something")).isEqualTo(unknownMedium);
  }

  @Test
  public void mapKnownMediums() {
    Medium hotWaterMedium = new Medium(0L, Medium.HOT_WATER);
    Medium districtCoolingMedium = new Medium(1L, Medium.DISTRICT_COOLING);
    MediumProvider mediumProvider = providerOf(Map.of(
      Medium.HOT_WATER, hotWaterMedium,
      Medium.DISTRICT_COOLING, districtCoolingMedium
    ));

    assertThat(mapToEvoMedium(mediumProvider, "Hot water")).isEqualTo(hotWaterMedium);
    assertThat(mapToEvoMedium(mediumProvider, "District cooling")).isEqualTo(districtCoolingMedium);
  }

  @Test
  public void mapKnownDistrictHeatingMediumAliases() {
    Medium districtHeatingMedium = new Medium(0L, Medium.DISTRICT_HEATING);
    MediumProvider mediumProvider = providerOf(Map.of(
      Medium.DISTRICT_HEATING,
      districtHeatingMedium
    ));

    asList(
      "District heating",
      "Heat, Return temp",
      "Heat, Flow temp",
      "HeatCoolingLoadMeter",
      "HeatFlow Temp",
      "HeatReturn Temp"
    )
      .forEach(medium ->
        assertThat(mapToEvoMedium(mediumProvider, medium))
          .isEqualTo(districtHeatingMedium)
      );
  }

  @Test
  public void mapKnownDistrictCoolingMediumAliases() {
    Medium districtCoolingMedium = new Medium(0L, Medium.DISTRICT_COOLING);
    MediumProvider mediumProvider = providerOf(Map.of(
      Medium.DISTRICT_COOLING,
      districtCoolingMedium
    ));

    asList(
      "Cooling load meter, flow temp",
      "Cooling load meter, Return temp"
    )
      .forEach(medium ->
        assertThat(mapToEvoMedium(mediumProvider, medium))
          .isEqualTo(districtCoolingMedium)
      );
  }

  @Test
  public void districtHeatingMeterIsMappedFromValueQuantities() {
    List<ValueDto> values = asList(
      newValueDto("Return temp."),
      newValueDto("Difference temp."),
      newValueDto("Flow temp."),
      newValueDto("Volume flow"),
      newValueDto("Power"),
      newValueDto("Volume"),
      newValueDto("Energy")
    );

    assertThat(resolveMedium(values)).isEqualTo(Medium.DISTRICT_HEATING);
  }

  private MediumProvider providerOf(Map<String, Medium> mediumMap) {
    return (name) -> Optional.ofNullable(mediumMap.get(name));
  }

  private static ValueDto newValueDto(String quantity) {
    return new ValueDto(LocalDateTime.now(), 0.0, "one", quantity);
  }
}
