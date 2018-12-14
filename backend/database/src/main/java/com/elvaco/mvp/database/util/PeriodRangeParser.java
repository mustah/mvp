package com.elvaco.mvp.database.util;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    if (rangeString.isEmpty() || rangeString.length() < 2) {
      throw new MalformedPeriodRange(rangeString);
    }

    if (rangeString.equals(EMPTY)) {
      return PeriodRange.empty();
    }

    ZonedDateTime start = null;
    ZonedDateTime stop = null;

    BoundType leftMarker = parseBoundMarker(rangeString.charAt(0)).orElseThrow(() ->
      new MalformedPeriodRange(String.format(
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
    if (bounds[0] != null) {
      start = parseTimestamp(bounds[0]);
    }
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

  public static String format(PeriodRange u) {
    if (u.isEmpty()) {
      return EMPTY;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(u.start.isInclusive ? '[' : '(');

    sb.append(u.getStartDateTime()
      .map(ZonedDateTime::toOffsetDateTime)
      .map(PostgresTimestampParser::format)
      .orElse(""));
    sb.append(',');
    sb.append(u.getStopDateTime()
      .map(ZonedDateTime::toOffsetDateTime)
      .map(PostgresTimestampParser::format)
      .orElse(""));
    sb.append(u.stop.isInclusive ? ']' : ')');
    return sb.toString();
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

    public static Optional<BoundType> fromChar(char c) {
      return Arrays.stream(values()).filter(v -> v.render == c).findAny();
    }

  }

  static class PostgresTimestampParser {

    private static final DateTimeFormatter POSTGRES_DATE_TIME_PARSE_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss[.SSS][xxx][xx][X]");

    private static final DateTimeFormatter POSTGRES_DATE_TIME_FMT_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSxxx");

    static String format(OffsetDateTime dateTime) {
      return dateTime.format(POSTGRES_DATE_TIME_FMT_FORMATTER);
    }

    static OffsetDateTime parse(String string) {
      return OffsetDateTime.parse(
        string,
        POSTGRES_DATE_TIME_PARSE_FORMATTER
      );
    }
  }
}
