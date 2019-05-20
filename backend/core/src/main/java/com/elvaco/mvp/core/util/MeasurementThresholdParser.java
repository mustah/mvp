package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeasurementThresholdParser {

  private static final Set<String> VALID_OPERATORS = Arrays.stream(Operator.values())
    .map(Operator::getSymbol)
    .collect(toSet());

  private static final Pattern THRESHOLD_FILTER_PATTERN = Pattern.compile(
    "(?<quantity>[^<>=]+)"
      + "\\s*(?<operator>" + String.join("|", VALID_OPERATORS) + ")"
      + "\\s*(?<value>[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)"
      + "\\s*(?<unit>\\S+)"
      + "\\s*(for (?<duration>\\d+) days)?$");

  private final QuantityProvider quantityProvider;
  private final UnitConverter unitConverter;

  public MeasurementThreshold parse(String thresholdExpr) {
    Matcher m = THRESHOLD_FILTER_PATTERN.matcher(thresholdExpr.trim());
    if (!m.matches()) {
      throw new IllegalArgumentException(String.format(
        "Malformed expression '%s' for measurement threshold",
        thresholdExpr
      ));
    }

    Quantity quantity = parseQuantity(m.group("quantity"));
    Operator operator = parseOperator(m.group("operator"));
    Duration duration = parseDuration(m.group("duration"));
    MeasurementUnit parsedUnitValue = new MeasurementUnit(
      parseUnit(m.group("unit")),
      parseValue(m.group("value"))
    );
    if (!unitConverter.isSameDimension(parsedUnitValue.getUnit(), quantity.storageUnit)) {
      throw new IllegalArgumentException(String.format(
        "Invalid unit '%s' for quantity '%s' in measurement threshold",
        parsedUnitValue.getUnit(),
        quantity.name
      ));
    }

    MeasurementUnit convertedUnitValue = unitConverter.convert(
      parsedUnitValue,
      quantity.storageUnit
    );

    return new MeasurementThreshold(
      quantity, parsedUnitValue, convertedUnitValue, operator, duration
    );
  }

  @Nullable
  private Duration parseDuration(@Nullable String dayCountStr) {
    if (dayCountStr == null) {
      return null;
    }
    long dayCount = 0;
    try {
      dayCount = Long.parseLong(dayCountStr);
    } catch (NumberFormatException ignore) {
    }

    if (dayCount <= 0) {
      throw new IllegalArgumentException(String.format(
        "Invalid duration '%s'",
        dayCountStr
      ));
    }
    return Duration.ofDays(Long.parseLong(dayCountStr));
  }

  private String parseUnit(String unit) {
    return unit;
  }

  private double parseValue(String valueString) {
    try {
      return Double.parseDouble(valueString);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException(String.format(
        "Invalid value '%s' for measurement threshold",
        valueString
      ));
    }
  }

  private Operator parseOperator(String operator) {
    return Operator.from(operator).orElseThrow(
      () -> new IllegalArgumentException(String.format(
        "Invalid operator '%s' for measurement threshold",
        operator
      ))
    );
  }

  private Quantity parseQuantity(String quantityName) {
    String trim = quantityName.trim();
    return quantityProvider.getByName(trim)
      .orElseThrow(
        () -> new IllegalArgumentException(String.format(
          "Invalid quantity '%s' for measurement threshold",
          trim
        )));
  }
}
