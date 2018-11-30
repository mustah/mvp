package com.elvaco.mvp.core.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeasurementThresholdParser {

  private static final Set<String> VALID_OPERATORS = Arrays.stream(Operator.values())
    .map(Operator::getSymbol)
    .collect(Collectors.toSet());

  private static final Pattern THRESHOLD_FILTER_PATTERN = Pattern.compile(
    "(?<quantity>[^<>=]+)"
      + "\\s*(?<operator>" + String.join("|", VALID_OPERATORS) + ")"
      + "\\s*(?<value>[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)"
      + "\\s*(?<unit>\\S+)$");
  private final QuantityProvider quantityProvider;
  private final UnitConverter unitConverter;

  public MeasurementThreshold parse(String thresholdExpr) {
    Matcher m = THRESHOLD_FILTER_PATTERN.matcher(thresholdExpr.trim());
    if (!m.matches()) {
      return null;
    }

    Quantity quantity = parseQuantity(m.group("quantity"));
    Operator operator = parseOperator(m.group("operator"));
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
      quantity, parsedUnitValue, convertedUnitValue, operator
    );
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
    return Optional.ofNullable(quantityProvider.getByName(trim))
      .orElseThrow(
        () -> new IllegalArgumentException(String.format(
          "Invalid quantity '%s' for measurement threshold",
          trim
        )));
  }
}
