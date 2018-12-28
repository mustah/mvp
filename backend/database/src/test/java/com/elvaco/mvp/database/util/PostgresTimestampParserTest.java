package com.elvaco.mvp.database.util;

import java.time.OffsetDateTime;

import com.elvaco.mvp.database.util.PeriodRangeParser.PostgresTimestampParser;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresTimestampParserTest {

  @Test
  public void withHourMinuteOffset() {
    assertThat(PostgresTimestampParser.parse("2018-01-01 00:30:45+01:00"))
      .isEqualTo(OffsetDateTime.parse("2018-01-01T00:30:45+01"));
  }

  @Test
  public void withHourOffset() {
    assertThat(PostgresTimestampParser.parse("2018-01-01 00:30:45+00"))
      .isEqualTo(OffsetDateTime.parse("2018-01-01T00:30:45+00"));
  }

  @Test
  public void withMillisecondsAndHourOffset() {
    assertThat(PostgresTimestampParser.parse("2018-01-01 00:30:45.913+01"))
      .isEqualTo(OffsetDateTime.parse("2018-01-01T00:30:45.913+01"));
  }
}
