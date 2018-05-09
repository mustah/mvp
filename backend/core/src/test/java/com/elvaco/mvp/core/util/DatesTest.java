package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static org.assertj.core.api.Assertions.assertThat;

public class DatesTest {

  private static final TimeZone STOCKHOLM_TIME_ZONE = TimeZone.getTimeZone("Europe/Stockholm");

  // 1990-04-09 08:45:12
  private static final ZonedDateTime ZONED_DATE_TIME_STOCKHOLM =
    ZonedDateTime.of(1990, 4, 9, 8, 45, 12, 0, STOCKHOLM_TIME_ZONE.toZoneId());

  @Test
  public void shouldFormatTimeUtcFromStockholmTimeZone() {
    assertThat(formatUtc(ZONED_DATE_TIME_STOCKHOLM)).isEqualTo("1990-04-09T06:45:12Z");
  }

  @Test
  public void addOneHourAndGetUtcTime() {
    ZonedDateTime date = ZONED_DATE_TIME_STOCKHOLM.plusHours(1);

    assertThat(formatUtc(date)).isEqualTo("1990-04-09T07:45:12Z");
  }

  @Test
  public void createZonedDateTimeFromString() {
    ZonedDateTime zonedDateTime = utcZonedDateTimeOf("2018-02-12T14:14:25Z");
    assertThat(zonedDateTime.toString()).isEqualTo("2018-02-12T14:14:25Z[UTC]");
  }

  @Test
  public void format() {
    String utcTime = formatUtc(utcZonedDateTimeOf("2018-02-12T14:14:25Z"));
    assertThat(utcTime).isEqualTo("2018-02-12T14:14:25Z");
  }
}
