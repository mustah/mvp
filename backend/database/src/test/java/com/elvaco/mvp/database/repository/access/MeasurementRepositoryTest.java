package com.elvaco.mvp.database.repository.access;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import org.junit.Test;

import static com.elvaco.mvp.database.repository.access.MeasurementRepository.getIntervalStart;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementRepositoryTest {

  private static final ZonedDateTime DATE = ZonedDateTime.parse("2018-10-04T21:47:15.34Z");

  @Test
  public void intervalStartForDayInterval() {
    OffsetDateTime result = getIntervalStart(DATE, TemporalResolution.day);
    assertThat(result).isEqualTo(ZonedDateTime.parse("2018-10-04T00:00:00.000Z")
      .toOffsetDateTime());
  }

  @Test
  public void intervalStartForHourInterval() {
    OffsetDateTime result = getIntervalStart(DATE, TemporalResolution.hour);
    assertThat(result).isEqualTo(ZonedDateTime.parse("2018-10-04T21:00:00.000Z")
      .toOffsetDateTime());
  }

  @Test
  public void intervalStartForMonthInterval() {
    OffsetDateTime result = getIntervalStart(DATE, TemporalResolution.month);
    assertThat(result).isEqualTo(ZonedDateTime.parse("2018-10-01T00:00:00.000Z")
      .toOffsetDateTime());
  }
}
