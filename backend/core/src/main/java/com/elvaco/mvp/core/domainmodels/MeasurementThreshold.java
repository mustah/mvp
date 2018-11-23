package com.elvaco.mvp.core.domainmodels;

import java.util.Arrays;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class MeasurementThreshold {
  public final Quantity quantity;
  public final MeasurementUnit parsedValueUnit;
  public final MeasurementUnit convertedValueUnit;
  public final Operator operator;

  public double getConvertedValue() {
    return convertedValueUnit.getValue();
  }

  public String getParsedUnit() {
    return parsedValueUnit.getUnit();
  }

  public double getParsedValue() {
    return parsedValueUnit.getValue();
  }

  public enum Operator {
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">=");

    private final String symbol;

    Operator(String symbol) {
      this.symbol = symbol;
    }

    public static Optional<Operator> from(String symbol) {
      return Arrays.stream(Operator.values())
        .filter(operator -> operator.symbol.equals(symbol))
        .findFirst();
    }
  }
}
