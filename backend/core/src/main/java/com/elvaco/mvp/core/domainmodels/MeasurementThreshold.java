package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.util.Optional;
import javax.annotation.Nullable;

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
  @Nullable
  public final Duration duration;

  public MeasurementThreshold(
    Quantity quantity,
    MeasurementUnit parsedValueUnit,
    MeasurementUnit convertedValueUnit,
    Operator operator
  ) {
    this(quantity, parsedValueUnit, convertedValueUnit, operator, null);
  }

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

    public String getSymbol() {
      return symbol;
    }

    public static Optional<Operator> from(String symbol) {
      return switch (symbol) {
        case "<" -> Optional.of(LESS_THAN);
        case ">" -> Optional.of(GREATER_THAN);
        case "<=" -> Optional.of(LESS_THAN_OR_EQUAL);
        case ">=" -> Optional.of(GREATER_THAN_OR_EQUAL);
        default -> Optional.empty();
      };
    }
  }
}
