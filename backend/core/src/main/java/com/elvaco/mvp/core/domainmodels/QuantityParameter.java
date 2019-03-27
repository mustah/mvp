package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
public class QuantityParameter {

  private static final String QUANTITY_UNIT_DELIMITER = ":";
  public final String name;
  @Nullable
  public final String unit;
  @Nullable
  public final DisplayMode displayMode;

  QuantityParameter(String name) {
    this(name, null, null);
  }

  QuantityParameter(String name, String unit) {
    this(name, unit, null);
  }

  public static QuantityParameter of(String quantityUnitPair) {
    String[] parts = quantityUnitPair.split(QUANTITY_UNIT_DELIMITER);
    String quantityName = parts[0];
    if (quantityName.isEmpty() || parts.length > 3) {
      throw new RuntimeException(
        "Invalid quantity/unit pair/display mode: '" + quantityUnitPair + "'"
      );
    } else if (parts.length == 3) {
      return new QuantityParameter(
        quantityName,
        parts[1],
        DisplayMode.from(parts[2])
      );
    } else if (parts.length == 2) {
      return new QuantityParameter(quantityName, parts[1], null);
    } else {
      return new QuantityParameter(quantityName);
    }
  }

  public static QuantityParameter of(DisplayQuantity displayQuantity) {
    return new QuantityParameter(
      displayQuantity.quantity.name,
      displayQuantity.unit,
      displayQuantity.displayMode
    );
  }

  public boolean isConsumption() {
    return CONSUMPTION.equals(displayMode);
  }
}
