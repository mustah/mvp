package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class QuantityParameter {

  private static final String QUANTITY_UNIT_DELIMITER = ":";
  public String name;
  @Nullable
  public String unit;
  @Nullable
  public DisplayMode displayMode;

  public QuantityParameter(String name) {
    this(name, null);
  }

  public QuantityParameter(String name, String unit) {
    this(name, unit, null);
  }

  public QuantityParameter(String name, String unit, DisplayMode displayMode) {
    this.name = name;
    this.unit = unit;
    this.displayMode = displayMode;
  }

  public static QuantityParameter of(@Nonnull String quantityUnitPair) {
    String[] parts = quantityUnitPair.split(QUANTITY_UNIT_DELIMITER);
    String quantityName = parts[0];
    if (quantityName.isEmpty() || parts.length > 3) {
      throw new RuntimeException(
        "Invalid quantity/unit pair/display mode: '" + quantityUnitPair + "'"
      );
    } else if (parts.length == 3) {
      return new QuantityParameter(
        quantityName,
        "".equals(parts[1]) ? null : parts[1],
        DisplayMode.from(parts[2])
      );
    } else if (parts.length == 2) {
      return new QuantityParameter(quantityName, parts[1]);
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

  @JsonIgnore
  public boolean isConsumption() {
    return CONSUMPTION.equals(displayMode);
  }
}
