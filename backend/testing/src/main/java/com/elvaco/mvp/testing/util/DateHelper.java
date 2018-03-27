package com.elvaco.mvp.testing.util;

import java.time.Instant;
import java.time.ZonedDateTime;

import com.elvaco.mvp.core.util.Dates;

public final class DateHelper {

  private DateHelper() {}

  /**
   * Creates a {@link ZonedDateTime} from an ISO_INSTANT string format.
   *
   * @see Instant#parse(CharSequence)
   */
  public static ZonedDateTime utcZonedDateTimeOf(String text) {
    return ZonedDateTime.ofInstant(Instant.parse(text), Dates.UTC.toZoneId());
  }
}
