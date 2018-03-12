package com.elvaco.mvp.database.repository.queryfilters;

import java.time.Instant;
import java.util.Date;

final class FilterUtils {

  static final String BEFORE = "before";
  static final String AFTER = "after";

  private FilterUtils() {}

  static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }
}
