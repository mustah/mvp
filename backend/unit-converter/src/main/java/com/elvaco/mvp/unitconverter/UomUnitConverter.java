package com.elvaco.mvp.unitconverter;

import java.util.HashMap;
import java.util.Map;
import javax.measure.Quantity;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.format.ParserException;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.exception.UnitConversionError;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import tec.units.ri.AbstractUnit;
import tec.units.ri.format.SimpleUnitFormat;
import tec.units.ri.function.RationalConverter;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.AlternateUnit;
import tec.units.ri.unit.MetricPrefix;
import tec.units.ri.unit.TransformedUnit;
import tec.units.ri.unit.Units;

import static java.util.Comparator.comparingInt;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

public class UomUnitConverter implements UnitConverter {

  private static final Map<String, String> REPLACEMENTS = new HashMap<>();
  private static final Unit<Energy> WATTHOUR = new TransformedUnit<>(
    "Wh",
    Units.JOULE,
    new RationalConverter(3600, 1)
  );
  private static final Unit<Pressure> BAR = new TransformedUnit<>(
    "bar",
    Units.PASCAL,
    new RationalConverter(100000, 1)
  );
  private static final Unit<Dimensionless> DIMENSIONLESS = new AlternateUnit<>(
    AbstractUnit.ONE,
    "Dimensionless"
  );
  private static final Unit<Dimensionless> RESERVED = new AlternateUnit<>(
    AbstractUnit.ONE,
    "Reserved"
  );
  private static final Unit<Dimensionless> UNKNOWN = new AlternateUnit<>(
    AbstractUnit.ONE,
    "Unknown"
  );
  private static final Unit<Power> VAR = Units.VOLT.multiply(Units.AMPERE).asType(Power.class);
  private static final Unit<Energy> VARH = VAR.multiply(Units.HOUR).asType(Energy.class);

  static {
    var aliases = Map.ofEntries(
      entry("Bar", BAR),
      entry("GWh", MetricPrefix.GIGA(WATTHOUR)),
      entry("kliter", MetricPrefix.KILO(Units.LITRE)),
      entry("kVAr", MetricPrefix.KILO(VAR)),
      entry("kvar", MetricPrefix.KILO(VAR)),
      entry("kVARh", MetricPrefix.KILO(VARH)),
      entry("kVArh", MetricPrefix.KILO(VARH)),
      entry("kvarh", MetricPrefix.KILO(VARH)),
      entry("kWh", MetricPrefix.KILO(WATTHOUR)),
      entry("MVAr", MetricPrefix.MEGA(VAR)),
      entry("Mvar", MetricPrefix.MEGA(VAR)),
      entry("MVARh", MetricPrefix.MEGA(VARH)),
      entry("MVArh", MetricPrefix.MEGA(VARH)),
      entry("Mvarh", MetricPrefix.MEGA(VARH)),
      entry("MWh", MetricPrefix.MEGA(WATTHOUR)),
      entry("TWh", MetricPrefix.TERA(WATTHOUR)),
      entry("Celsius", Units.CELSIUS),
      entry("Days", Units.DAY),
      entry("Hours", Units.HOUR),
      entry("Kelvin", Units.KELVIN),
      entry("liter", Units.LITRE),
      entry("Minutes", Units.MINUTE),
      entry("Seconds", Units.SECOND),
      entry("sec", Units.SECOND),
      entry("var", VAR),
      entry("VARh", VARH),
      entry("varh", VARH)
    );

    var labels = Map.ofEntries(
      entry("bar", BAR),
      entry("Dimensionless", DIMENSIONLESS),
      entry("Reserved", RESERVED),
      entry("Unknown", UNKNOWN),
      entry("VAr", VAR),
      entry("VArh", VARH),
      entry("Wh", WATTHOUR)
    );

    var instance = SimpleUnitFormat.getInstance();

    aliases.forEach((input, target) -> instance.alias(target, input));
    labels.forEach((input, target) -> instance.label(target, input));

    REPLACEMENTS.put("m3", "m³");
    REPLACEMENTS.put("second(s)", "s");
    REPLACEMENTS.put("minute(s)", "min");
    REPLACEMENTS.put("hour(s)", "h");
    REPLACEMENTS.put("day(s)", "day");
    REPLACEMENTS.put("*", "");
  }

  @Override
  public MeasurementUnit toMeasurementUnit(String valueAndUnit, String target) {
    valueAndUnit = replace(valueAndUnit);
    target = replace(target);

    Quantity<?> sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit);
    } catch (ParserException | IllegalArgumentException iex) {
      throw new UnitConversionError(iex.getMessage());
    }

    Quantity<?> resultQuantity;
    try {
      Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);
      resultQuantity = sourceQuantity.to(targetUnit);
    } catch (ParserException ex) {
      throw UnitConversionError.unknownUnit(target);
    } catch (UnconvertibleException ex) {
      throw new UnitConversionError(valueAndUnit, target);
    }
    return new MeasurementUnit(
      resultQuantity.getUnit().toString(),
      resultQuantity.getValue().doubleValue()
    );
  }

  @Override
  public MeasurementUnit toMeasurementUnit(
    MeasurementUnit measurementUnit, String targetUnit
  ) {
    return toMeasurementUnit(measurementUnit.toString(), targetUnit);
  }

  public boolean isSameDimension(String firstUnit, String secondUnit) {
    var instance = SimpleUnitFormat.getInstance();
    Unit<?> cleanedUnit;
    Unit<?> cleanedSecondUnit;

    try {
      cleanedUnit = instance.parse(replace(firstUnit));
      cleanedSecondUnit = instance.parse(replace(secondUnit));
    } catch (ParserException ex) {
      return false;
    }

    return cleanedUnit.isCompatible(cleanedSecondUnit);
  }

  @Override
  public double toValue(double value, String fromUnit, String toUnit) {
    return toMeasurementUnit(value + " " + fromUnit, toUnit).getValue();
  }

  private static String replace(String valueAndUnit) {
    var replacements = REPLACEMENTS.keySet()
      .stream()
      .sorted(comparingInt(String::length))
      .collect(toList());

    for (var replacement : replacements) {
      valueAndUnit = valueAndUnit.replace(replacement, REPLACEMENTS.get(replacement));
    }
    return valueAndUnit;
  }
}
