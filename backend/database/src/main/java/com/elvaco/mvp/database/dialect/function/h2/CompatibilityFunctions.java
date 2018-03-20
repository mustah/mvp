package com.elvaco.mvp.database.dialect.function.h2;

import java.util.HashMap;
import java.util.Map;
import javax.measure.Quantity;
import javax.measure.Unit;

import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import lombok.extern.slf4j.Slf4j;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

@SuppressWarnings("WeakerAccess") // Used by H2 DB
@Slf4j
public final class CompatibilityFunctions {

  private static final Map<String, Unit<?>> CUSTOM_TYPES = new HashMap<>();

  private CompatibilityFunctions() {}

  public static MeasurementUnit toMeasurementUnit(String valueAndUnit, String target) {
    Quantity<?> sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit);
    } catch (IllegalArgumentException iex) {
      MeasurementUnit measurementUnit = MeasurementUnit.from(valueAndUnit);
      if (CUSTOM_TYPES.containsKey(measurementUnit.getUnit())) {
        sourceQuantity = Quantities.getQuantity(
          measurementUnit.getValue(),
          CUSTOM_TYPES.get(measurementUnit.getUnit())
        );
      } else {
        throw iex;
      }
    }
    Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);
    Quantity<?> resultQuantity = sourceQuantity.to(targetUnit);

    return new MeasurementUnit(
      resultQuantity.getUnit().toString(),
      resultQuantity.getValue().doubleValue()
    );
  }

  @SuppressWarnings("WeakerAccess") // It's used in h2 provisioning loader
  public static String unitAt(String valueAndUnit, String target) {
    return toMeasurementUnit(valueAndUnit, target).toString();
  }

  static {
    SimpleUnitFormat.getInstance().alias(Units.CELSIUS, "Celsius");
    SimpleUnitFormat.getInstance().alias(Units.KELVIN, "Kelvin");
    /* Necessary hack, because UOM's unit parser doesn't approve of
    this unit format.*/
    CUSTOM_TYPES.put("m3", Units.CUBIC_METRE);
  }
}
