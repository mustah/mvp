package com.elvaco.mvp.database.util;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.exception.MixedDimensionForMeterQuantity;
import com.elvaco.mvp.core.exception.UnitConversionError;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.hibernate.JDBCException;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.core.exception.UnitConversionError.unknownUnit;
import static java.util.Arrays.asList;

@UtilityClass
public class SqlErrorMapper {

  private static final Pattern DIMENSION_MISMATCH_ERROR_RE =
    Pattern.compile("^.*dimension mismatch in \"@\" operation: \"\\d+\\.\\d+ (.*)\",.*$");

  private static final Pattern UNKNOWN_UNIT_ERROR_RE =
    Pattern.compile(".*unit .* is not known.*");
  private static final Pattern MIXED_DIMENSIONS_ERROR_RE = Pattern.compile(
    ".*Mixed dimensions for same quantity/meter combination is not allowed \\(have 1 (.*), got 1 ("
      + ".*)\\).*", Pattern.DOTALL);

  private static final List<ErrorPatternMapperContainer> PATTERN_MAPPER_CONTAINERS =
    Collections.unmodifiableList(
      asList(
        ErrorPatternMapperContainer.newFunctionalMapperContainer(
          MIXED_DIMENSIONS_ERROR_RE,
          SqlErrorMapper::mapMixedDimensionsError
        ),
        ErrorPatternMapperContainer.newBiFunctionalMapperContainer(
          UNKNOWN_UNIT_ERROR_RE,
          SqlErrorMapper::mapUnknownUnitError
        ),
        ErrorPatternMapperContainer.newBiFunctionalMapperContainer(
          DIMENSION_MISMATCH_ERROR_RE,
          SqlErrorMapper::mapDimensionMismatchError
        )
      )
    );

  public static RuntimeException mapDataIntegrityViolation(DataIntegrityViolationException ex) {
    return mapDataIntegrityViolation(ex, null);
  }

  private static RuntimeException mapDataIntegrityViolation(
    DataIntegrityViolationException ex,
    String extraInformation
  ) {
    Throwable cause = ex.getCause();
    if (cause instanceof JDBCException jdbcException) {
      String sqlErrorMessage = jdbcException.getSQLException().getMessage();
      for (ErrorPatternMapperContainer patternMapperContainer : PATTERN_MAPPER_CONTAINERS) {
        Matcher matcher = patternMapperContainer.pattern.matcher(sqlErrorMessage);
        if (matcher.matches()) {
          MatchResult matchResult = matcher.toMatchResult();
          if (patternMapperContainer.mapperBiFunction != null) {
            return patternMapperContainer.mapperBiFunction.apply(matchResult, extraInformation);
          } else if (patternMapperContainer.mapperFunction != null) {
            return patternMapperContainer.mapperFunction.apply(matchResult);
          }
        }
      }
    }
    return ex;
  }

  private static RuntimeException mapDimensionMismatchError(MatchResult matchResult, String scale) {
    return new UnitConversionError(matchResult.group(1), scale);
  }

  private static RuntimeException mapUnknownUnitError(MatchResult matchResult, String scale) {
    return unknownUnit(scale);
  }

  private static RuntimeException mapMixedDimensionsError(MatchResult matchResult) {
    return new MixedDimensionForMeterQuantity(matchResult.group(1), matchResult.group(2));
  }

  @RequiredArgsConstructor
  private static class ErrorPatternMapperContainer {

    final Pattern pattern;
    @Nullable
    final Function<MatchResult, RuntimeException> mapperFunction;
    @Nullable
    final BiFunction<MatchResult, String, RuntimeException> mapperBiFunction;

    static ErrorPatternMapperContainer newBiFunctionalMapperContainer(
      Pattern pattern,
      BiFunction<MatchResult, String, RuntimeException> mapperBiFunction
    ) {
      return new ErrorPatternMapperContainer(pattern, null, mapperBiFunction);
    }

    static ErrorPatternMapperContainer newFunctionalMapperContainer(
      Pattern pattern,
      Function<MatchResult, RuntimeException> mapperFunction
    ) {
      return new ErrorPatternMapperContainer(pattern, mapperFunction, null);
    }
  }
}
