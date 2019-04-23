package com.elvaco.mvp.unitconverter;

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

import static java.util.Map.entry;

public class UomUnitConverter implements UnitConverter {

  private static final Map<String, String> REPLACEMENTS = Map.of(
    "m3", "m³",
    "second(s)", "s",
    "minute(s)", "min",
    "hour(s)", "h",
    "day(s)", "day",
    "*", ""
  );

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
  }

  @Override
  public MeasurementUnit convert(
    MeasurementUnit measurementUnit, String targetUnit
  ) {
    return convertString(measurementUnit.toString(), targetUnit);
  }

  @Override
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

  private MeasurementUnit convertString(String valueAndUnit, String target) {
    valueAndUnit = replace(valueAndUnit);
    target = replace(target);

    Quantity<?> sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit);
    } catch (ParserException | IllegalArgumentException iex) {
      throw new UnitConversionError(iex.getMessage());
    }

    try {
      /* FIXME: How do we convert between two unknown types "safely"? */
      @SuppressWarnings("rawtypes")
      Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);
      @SuppressWarnings("unchecked")
      Quantity<?> resultQuantity = sourceQuantity.to(targetUnit);

      return new MeasurementUnit(
        resultQuantity.getUnit().toString(),
        resultQuantity.getValue().doubleValue()
      );
    } catch (ParserException ex) {
      throw UnitConversionError.unknownUnit(target);
    } catch (UnconvertibleException ex) {
      throw new UnitConversionError(valueAndUnit, target);
    }
  }

  private static String replace(String valueAndUnit) {
    for (var replacement : REPLACEMENTS.entrySet()) {
      valueAndUnit = valueAndUnit.replace(replacement.getKey(), replacement.getValue());
    }
    return valueAndUnit;
  }
}
