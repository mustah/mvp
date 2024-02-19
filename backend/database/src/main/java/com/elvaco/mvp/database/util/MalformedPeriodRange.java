package com.elvaco.mvp.database.util;

import java.io.Serial;
import java.time.format.DateTimeParseException;

class MalformedPeriodRange extends IllegalArgumentException {
  @Serial private static final long serialVersionUID = -4671226134949727994L;

  MalformedPeriodRange(String rangeString) {
    super(rangeString);
  }

  MalformedPeriodRange(String message, DateTimeParseException cause) {
    super(message, cause);
  }
}
