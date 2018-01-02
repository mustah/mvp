package com.elvaco.mvp.dialect.function.h2;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;

import com.elvaco.mvp.entity.measurement.MeasurementUnit;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public class CompatibilityFunctions {

  private static Map<String, Unit> customTypes = new HashMap<>();

  static {
    SimpleUnitFormat.getInstance().alias(Units.CELSIUS, "Celsius");
    SimpleUnitFormat.getInstance().alias(Units.KELVIN, "Kelvin");
    /* Necessary hack, because UOM's unit parser doesn't approve of
    this unit format.*/
    customTypes.put("m3", Units.CUBIC_METRE);
  }

  public static String unitAt(String valueAndUnit, String target) {
    Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);

    Quantity sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit.toString());
    } catch (IllegalArgumentException iex) {
      MeasurementUnit munit = new MeasurementUnit(valueAndUnit);
      if (customTypes.containsKey(munit.getUnit())) {
        sourceQuantity = Quantities.getQuantity(
          munit.getValue(),
          customTypes.get(munit.getUnit())
        );
      } else {
        throw iex;
      }
    }
    Quantity resultQuantity = sourceQuantity.to(targetUnit);

    return new MeasurementUnit(
      resultQuantity.getUnit().toString(),
      resultQuantity.getValue().doubleValue()
    ).toString();
  }
}
