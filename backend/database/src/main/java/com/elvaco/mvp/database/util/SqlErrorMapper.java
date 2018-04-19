package com.elvaco.mvp.database.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.elvaco.mvp.core.exception.UnitConversionError;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SqlErrorMapper {

  private static final Pattern DIMENSION_MISMATCH_ERROR_RE =
    Pattern.compile("^.*dimension mismatch in \"@\" operation: \"\\d+\\.\\d+ (.*)\",.*$");

  private static final Pattern UNKNOWN_UNIT_ERROR_RE =
    Pattern.compile(".*unit .* is not known.*");

  public static Optional<RuntimeException> mapScalingError(
    String scale,
    String sqlErrorMessage
  ) {
    Matcher matcher = DIMENSION_MISMATCH_ERROR_RE.matcher(sqlErrorMessage);
    if (matcher.matches()) {
      return Optional.of(new UnitConversionError(matcher.group(1), scale));
    } else if (UNKNOWN_UNIT_ERROR_RE.matcher(sqlErrorMessage).matches()) {
      return Optional.of(new UnitConversionError(scale));
    }
    return Optional.empty();
  }
}
