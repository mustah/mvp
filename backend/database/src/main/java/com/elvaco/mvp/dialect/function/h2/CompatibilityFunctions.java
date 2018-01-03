package com.elvaco.mvp.dialect.function.h2;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;

import com.elvaco.mvp.entity.measurement.MeasurementUnit;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public final class CompatibilityFunctions {

  private static final Map<String, Unit<?>> CUSTOM_TYPES = new HashMap<>();

  private CompatibilityFunctions() {}

  static {
    SimpleUnitFormat.getInstance().alias(Units.CELSIUS, "Celsius");
    SimpleUnitFormat.getInstance().alias(Units.KELVIN, "Kelvin");
    /* Necessary hack, because UOM's unit parser doesn't approve of
    this unit format.*/
    CUSTOM_TYPES.put("m3", Units.CUBIC_METRE);
  }

  public static String unitAt(String valueAndUnit, String target) {
    Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);

    Quantity<?> sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit);
    } catch (IllegalArgumentException iex) {
      MeasurementUnit measurementUnit = new MeasurementUnit(valueAndUnit);
      if (CUSTOM_TYPES.containsKey(measurementUnit.getUnit())) {
        sourceQuantity = Quantities.getQuantity(
          measurementUnit.getValue(),
          CUSTOM_TYPES.get(measurementUnit.getUnit())
        );
      } else {
        throw iex;
      }
    }
    Quantity<?> resultQuantity = sourceQuantity.to(targetUnit);

    return new MeasurementUnit(
      resultQuantity.getUnit().toString(),
      resultQuantity.getValue().doubleValue()
    ).toString();
  }
}
