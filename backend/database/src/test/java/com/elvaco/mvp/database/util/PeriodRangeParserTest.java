package com.elvaco.mvp.database.util;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.PeriodRange;

import org.junit.Test;

import static com.elvaco.mvp.database.util.PeriodRangeParser.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PeriodRangeParserTest {

  @Test
  public void parseUnboundedRange() {
    assertThat(PeriodRangeParser.parse("[]")).isEqualTo(PeriodRange.closedFrom(null, null));

    assertThat(PeriodRangeParser.parse("()")).isEqualTo(PeriodRange.openFrom(null, null));

    assertThat(PeriodRangeParser.parse("[)")).isEqualTo(PeriodRange.halfOpenFrom(null, null));
  }

  @Test
  public void formatUnbounded() {
    assertThat(PeriodRangeParser.format(PeriodRange.openFrom(null, null)))
      .isEqualTo("(,)");

    assertThat(PeriodRangeParser.format(PeriodRange.closedFrom(null, null)))
      .isEqualTo("(,)");

    assertThat(PeriodRangeParser.format(PeriodRange.halfOpenFrom(null, null)))
      .isEqualTo("(,)");
  }

  @Test
  public void parseEmptyString() {
    assertThatThrownBy(() -> PeriodRangeParser.parse("")).isInstanceOf(MalformedPeriodRange.class);
  }

  @Test
  public void parseEmptyRange() {
    assertThat(PeriodRangeParser.parse(EMPTY)).isEqualTo(PeriodRange.empty());
  }

  @Test
  public void parseInvalidStrings() {
    assertMalformed("[[[[[[)");
    assertMalformed("fefefe");
    assertMalformed("[\"2018-01-01 08:00:00+01\"");
    assertMalformed("\"2018-01-01 08:00:00+01\"]");
    assertMalformed("\"],2018-01-01 08:00:00+01\"]");
    assertMalformed("],\"2018-01-01 08:00:00+01\"]");
    assertMalformed("[\"2018-01-01 08:00:00+01\",,\"2018-01-01 08:00:00+01\"]");
    assertMalformed("[\"2018-01-01 08:00:00+01\",\"2018-01-01 08:00:00+01\",]");
  }

  @Test
  public void parseHalfBoundedRanges() {
    assertThat(PeriodRangeParser.parse("[\"2019-01-08 15:01:50.97411+01\",)"))
      .isEqualTo(PeriodRange.halfOpenFrom(
        ZonedDateTime.parse("2019-01-08T15:01:50.97411+01"),
        null
      ));

    assertThat(PeriodRangeParser.parse("[\"2019-01-08 17:01:00.8583+01\",)"))
      .isEqualTo(PeriodRange.halfOpenFrom(
        ZonedDateTime.parse("2019-01-08T17:01:00.8583+01"),
        null
      ));

    assertThat(PeriodRangeParser.parse("[\"2019-01-08 15:01:50.974+01\",)"))
      .isEqualTo(PeriodRange.halfOpenFrom(
        ZonedDateTime.parse("2019-01-08T15:01:50.974+01"),
        null
      ));

    assertThat(PeriodRangeParser.parse("[\"2018-01-01 08:00:00+01\",)"))
      .isEqualTo(PeriodRange.halfOpenFrom(
        ZonedDateTime.parse("2018-01-01T08:00:00+01"),
        null
      ));

    assertThat(PeriodRangeParser.parse("[,\"2018-01-01 08:00:00+01\")"))
      .isEqualTo(PeriodRange.halfOpenFrom(
        null,
        ZonedDateTime.parse("2018-01-01T08:00:00+01")
      ));
  }

  @Test
  public void parseTimestamps_fractions() {
    assertThat(parseTimestamp("2019-01-08 15:01:50.97+01")).isEqualTo(ZonedDateTime.parse(
      "2019-01-08T15:01:50.97+01"));

    ZonedDateTime actual;

    actual = parseTimestamp("2019-01-08 15:01:50+01");
    assertThat(actual.getNano()).isEqualTo(0L);

    actual = parseTimestamp("2019-01-08 15:01:50.9+01");
    assertThat(actual.getNano()).isEqualTo(900000000L);

    actual = parseTimestamp("2019-01-08 15:01:50.98+01");
    assertThat(actual.getNano()).isEqualTo(980000000L);

    actual = parseTimestamp("2019-01-08 15:01:50.987+01");
    assertThat(actual.getNano()).isEqualTo(987000000L);

    actual = parseTimestamp("2019-01-08 15:01:50.9876+01");
    assertThat(actual.getNano()).isEqualTo(987600000L);

    actual = parseTimestamp("2019-01-08 15:01:50.98765+01");
    assertThat(actual.getNano()).isEqualTo(987650000L);

    actual = parseTimestamp("2019-01-08 15:01:50.987654+01");
    assertThat(actual.getNano()).isEqualTo(987654000L);

    actual = parseTimestamp("2019-01-08 15:01:50.9876543+01");
    assertThat(actual.getNano()).isEqualTo(987654300L);

    actual = parseTimestamp("2019-01-08 15:01:50.98765432+01");
    assertThat(actual.getNano()).isEqualTo(987654320L);

    actual = parseTimestamp("2019-01-08 15:01:50.987654321+01");
    assertThat(actual.getNano()).isEqualTo(987654321L);
  }

  @Test
  public void formatHalfBoundedRanges() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00+01");

    assertThat(PeriodRangeParser.format(PeriodRange.openFrom(dateTime, null)))
      .isEqualTo("(2018-01-01 08:00:00.000+01:00,)");

    assertThat(PeriodRangeParser.format(PeriodRange.halfOpenFrom(null, dateTime)))
      .isEqualTo("(,2018-01-01 08:00:00.000+01:00)");

    assertThat(PeriodRangeParser.format(PeriodRange.closedFrom(null, dateTime)))
      .isEqualTo("(,2018-01-01 08:00:00.000+01:00]");
  }

  @Test
  public void parseBoundedRanges() {
    ZonedDateTime start = ZonedDateTime.parse("2018-01-01T08:00:00+01");
    ZonedDateTime stop = ZonedDateTime.parse("2018-01-01T09:00:00+01");
    assertThat(PeriodRangeParser.parse("[\"2018-01-01 08:00:00+01\",\"2018-01-01 09:00:00+01\")"))
      .isEqualTo(PeriodRange.halfOpenFrom(start, stop));

    assertThat(PeriodRangeParser.parse("[\"2018-01-01 08:00:00+01\",\"2018-01-01 09:00:00+01\"]"))
      .isEqualTo(PeriodRange.closedFrom(start, stop));

    assertThat(PeriodRangeParser.parse("(\"2018-01-01 08:00:00+01\",\"2018-01-01 09:00:00+01\")"))
      .isEqualTo(PeriodRange.openFrom(start, stop));
  }

  @Test
  public void formatBoundedRanges() {
    ZonedDateTime start = ZonedDateTime.parse("2018-01-01T08:00:00+01");
    ZonedDateTime stop = ZonedDateTime.parse("2018-01-01T09:00:00+01");

    assertThat(PeriodRangeParser.format(PeriodRange.openFrom(start, stop)))
      .isEqualTo("(2018-01-01 08:00:00.000+01:00,2018-01-01 09:00:00.000+01:00)");

    assertThat(PeriodRangeParser.format(PeriodRange.halfOpenFrom(start, stop)))
      .isEqualTo("[2018-01-01 08:00:00.000+01:00,2018-01-01 09:00:00.000+01:00)");

    assertThat(PeriodRangeParser.format(PeriodRange.closedFrom(start, stop)))
      .isEqualTo("[2018-01-01 08:00:00.000+01:00,2018-01-01 09:00:00.000+01:00]");
  }

  @Test
  public void formatEmptyRange() {
    assertThat(PeriodRangeParser.format(PeriodRange.empty())).isEqualTo(EMPTY);
  }

  private void assertMalformed(String rangeString) {
    assertThatThrownBy(() -> PeriodRangeParser.parse(rangeString))
      .isInstanceOf(MalformedPeriodRange.class);
  }

  private static ZonedDateTime parseTimestamp(String ts) {
    return PeriodRangeParser.parse(String.format(
      "[\"%s\",)",
      ts
    )).getStartDateTime().orElseThrow();
  }
}
