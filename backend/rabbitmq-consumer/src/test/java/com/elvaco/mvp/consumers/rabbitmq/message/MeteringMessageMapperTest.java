package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mapToEvoMedium;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMeterDefinition;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageMapperTest {

  @Test
  public void unknownMediumIsMappedFromEmptyValueSet() {
    assertThat(resolveMeterDefinition(emptyList())).isEqualTo(MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void unknownMediumIsMappedFromUnknownValueSet() {
    List<ValueDto> values = singletonList(
      new ValueDto(LocalDateTime.now(), 0.0, "MW", "UnknownQuantity")
    );

    assertThat(resolveMeterDefinition(values)).isEqualTo(MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void bothUnknownAndKnownQuantitiesAreMappedToUnknownMedium() {
    List<ValueDto> values = asList(
      newValueDto("Volume"),
      newValueDto("Bluahe")
    );

    assertThat(resolveMeterDefinition(values)).isEqualTo(MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void mapColdWaterMediumAsWater() {
    assertThat(mapToEvoMedium("Cold water")).isEqualTo("Water");
  }

  @Test
  public void mapMeteringMediumToInputMediumString() {
    assertThat(mapToEvoMedium("Something")).isEqualTo("Something");
  }

  @Test
  public void mapKnownMediums() {
    assertThat(mapToEvoMedium("Hot water")).isEqualTo(Medium.HOT_WATER.medium);
    assertThat(mapToEvoMedium("District cooling")).isEqualTo(Medium.DISTRICT_COOLING.medium);
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

    assertThat(resolveMeterDefinition(values)).isEqualTo(MeterDefinition.DISTRICT_HEATING_METER);
  }

  private static ValueDto newValueDto(String quantity) {
    return new ValueDto(LocalDateTime.now(), 0.0, "one", quantity);
  }
}
