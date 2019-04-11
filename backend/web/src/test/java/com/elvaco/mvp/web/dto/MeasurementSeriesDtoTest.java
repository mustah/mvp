package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeasurementValue;

import org.junit.Test;

import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSortedMeasurements;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementSeriesDtoTest {

  @Test
  public void valuesAreOrderedByWhen() {
    ZonedDateTime now = ZonedDateTime.now();
    List<MeasurementValue> values = List.of(
      new MeasurementValue(1.0, now.toInstant()),
      new MeasurementValue(1.0, now.minusDays(1).toInstant()),
      new MeasurementValue(1.0, now.plusMinutes(12).toInstant())
    );

    assertThat(toSortedMeasurements(values)).containsExactly(
      new MeasurementValueDto(now.minusDays(1).toInstant(), 1.0),
      new MeasurementValueDto(now.toInstant(), 1.0),
      new MeasurementValueDto(now.plusMinutes(12).toInstant(), 1.0)
    );
  }
}
