package com.elvaco.mvp.database.util;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PeriodRangeParser {

  static final String EMPTY = "empty";

  public static PeriodRange parse(String rangeString) {
    if (rangeString.length() < 2) {
      throw new MalformedPeriodRange(rangeString);
    }

    if (rangeString.equals(EMPTY)) {
      return PeriodRange.empty();
    }

    BoundType leftMarker = parseBoundMarker(rangeString.charAt(0))
      .orElseThrow(() -> new MalformedPeriodRange(String.format(
        "Missing left bound marker in period range '%s'",
        rangeString
      )));
    if (!leftMarker.isLeftBound) {
      throw new MalformedPeriodRange(String.format(
        "Invalid left bound marker in period range '%s'",
        rangeString
      ));
    }

    String[] bounds = rangeString.substring(1, rangeString.length() - 1).split(",", -1);
    ZonedDateTime start = null;
    if (bounds[0] != null) {
      start = parseTimestamp(bounds[0]);
    }

    ZonedDateTime stop = null;
    if (bounds.length == 2) {
      stop = parseTimestamp(bounds[1]);
    } else if (bounds.length > 2) {
      throw new MalformedPeriodRange(String.format(
        "Too many commas in period range '%s'",
        rangeString
      ));
    }

    BoundType rightMarker = parseBoundMarker(rangeString.charAt(rangeString.length() - 1))
      .orElseThrow(() -> new MalformedPeriodRange(String.format(
        "Missing right bound marker in period range '%s'",
        rangeString
      )));

    if (rightMarker.isLeftBound) {
      throw new MalformedPeriodRange(String.format(
        "Invalid right bound marker in period range '%s'",
        rangeString
      ));
    }
    PeriodBound leftBound = leftMarker.isExclusive
      ? PeriodBound.exclusiveOf(start) :
      PeriodBound.inclusiveOf(start);
    PeriodBound rightBound = rightMarker.isExclusive
      ? PeriodBound.exclusiveOf(stop)
      : PeriodBound.inclusiveOf(stop);

    return new PeriodRange(leftBound, rightBound);
  }

  public static String format(PeriodRange pr) {
    return pr.isEmpty()
      ? EMPTY
      : formatStart(pr) + formatDateRange(pr) + formatStop(pr);
  }

  private static String formatDateRange(PeriodRange u) {
    return formatDateTime(u.getStartDateTime()) + ',' + formatDateTime(u.getStopDateTime());
  }

  private static char formatStart(PeriodRange u) {
    return u.start.isInclusive ? '[' : '(';
  }

  private static char formatStop(PeriodRange u) {
    return u.stop.isInclusive ? ']' : ')';
  }

  @Nullable
  private static ZonedDateTime parseTimestamp(@Nullable String str) {
    if (str == null || str.isEmpty()) {
      return null;
    }

    try {
      return PostgresTimestampParser.parse(stripQuotes(str)).toZonedDateTime();
    } catch (DateTimeParseException exc) {
      throw new MalformedPeriodRange("Failed to parse timestamp", exc);
    }
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static String formatDateTime(Optional<ZonedDateTime> zonedDateTime) {
    return zonedDateTime.map(ZonedDateTime::toOffsetDateTime)
      .map(PostgresTimestampParser::format)
      .orElse("");
  }

  private static String stripQuotes(String str) {
    return str.replaceAll("\"", "");
  }

  private static Optional<BoundType> parseBoundMarker(char markerChar) {
    return BoundType.fromChar(markerChar);
  }

  @RequiredArgsConstructor
  enum BoundType {
    LEFT_INCLUSIVE('[', true, false),
    LEFT_EXCLUSIVE('(', true, true),
    RIGHT_INCLUSIVE(']', false, false),
    RIGHT_EXCLUSIVE(')', false, true);

    public final char render;
    public final boolean isLeftBound;
    public final boolean isExclusive;

    private static Optional<BoundType> fromChar(char c) {
      return Arrays.stream(values()).filter(v -> v.render == c).findAny();
    }

  }

  @UtilityClass
  static final class PostgresTimestampParser {

    private static final DateTimeFormatter POSTGRES_DATE_TIME_PARSE_FORMATTER =
      new DateTimeFormatterBuilder()
        .appendPattern("uuuu-MM-dd HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .appendPattern("[xxx][xx][X]")
        .toFormatter();

    private static final DateTimeFormatter POSTGRES_DATE_TIME_FMT_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSxxx");

    static OffsetDateTime parse(String string) {
      return OffsetDateTime.parse(string, POSTGRES_DATE_TIME_PARSE_FORMATTER);
    }

    private static String format(OffsetDateTime dateTime) {
      return dateTime.format(POSTGRES_DATE_TIME_FMT_FORMATTER);
    }
  }
}
