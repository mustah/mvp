package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import org.junit.Test;

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

  @Test
  public void gasMeterIsMappedFromValueQuantities() {
    List<ValueDto> values = asList(
      newValueDto("Volume")
    );

    assertThat(resolveMeterDefinition(values)).isEqualTo(MeterDefinition.GAS_METER);
  }

  private static ValueDto newValueDto(String quantity) {
    return new ValueDto(LocalDateTime.now(), 0.0, "one", quantity);
  }
}
