package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementSeriesDtoTest {

  @Test
  public void valuesAreOrderedByWhen() {
    ZonedDateTime now = ZonedDateTime.now();
    List<MeasurementValueDto> values = asList(
      new MeasurementValueDto(now.toInstant(), 1.0),
      new MeasurementValueDto(now.minusDays(1).toInstant(), 1.0),
      new MeasurementValueDto(now.plusMinutes(12).toInstant(), 1.0)
    );

    assertThat(new MeasurementSeriesDto("quantity", "unit", "label",
      values
    ).values).containsExactly(
      new MeasurementValueDto(now.minusDays(1).toInstant(), 1.0),
      new MeasurementValueDto(now.toInstant(), 1.0),
      new MeasurementValueDto(now.plusMinutes(12).toInstant(), 1.0)
    );
  }

}
