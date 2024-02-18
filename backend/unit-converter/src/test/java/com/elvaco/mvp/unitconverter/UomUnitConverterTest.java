package com.elvaco.mvp.unitconverter;

import java.util.Map;
import java.util.TreeMap;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.exception.UnitConversionError;
import com.elvaco.mvp.core.unitconverter.UnitConverter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UomUnitConverterTest {

  private final UnitConverter converter = new UomUnitConverter();
  private final Map<String, String> meteringUnits = new TreeMap<>(Map.ofEntries(
    entry("sec", "1 s"),
    entry("Wh", "1 Wh"),
    entry("J", "1 J"),
    entry("m^3", "1 ㎥"),
    entry("m^3/h", "1 m³/h"),
    entry("kg", "1 kg"),
    entry("W", "1 W"),
    entry("J/h", "1 J/h"),
    entry("kg/h", "1 kg/h"),
    entry("Kelvin", "1 K"),
    entry("Celsius", "1 °C"),
    entry("Bar", "1 bar"),
    entry("Dimensionless", "1 Dimensionless"),
    entry("Seconds", "1 s"),
    entry("Minutes", "1 min"),
    entry("Hours", "1 h"),
    entry("Days", "1 day"),
    entry("m^3/min", "1 m³/min"),
    entry("m^3/sec", "1 m³/s"),
    entry("Reserved", "1 Reserved"),
    entry("Unknown", "1 Unknown"),
    entry("kWh", "1 kWh"),
    entry("MWh", "1 MWh"),
    entry("kW", "1 kW"),
    entry("MW", "1 MW"),
    entry("l/s", "1 l/s"),
    entry("V", "1 V"),
    entry("kvar", "1 kVAr"),
    entry("Wh/k*liter", "1 Wh/kl"),
    entry("kg/liter", "1 kg/l"),
    entry("second(s)", "1 s"),
    entry("m3", "1 ㎥"),
    entry("m3/h", "1 m³/h"),
    entry("°C", "1 °C"),
    entry("K", "1 K"),
    entry("minute(s)", "1 min"),
    entry("hour(s)", "1 h"),
    entry("day(s)", "1 day"),
    entry("kVARh", "1 kVArh"),
    entry("A", "1 A"),
    entry("Wh/kg", "1 Wh/kg"),
    entry("l", "1 l"),
    //    entry("dBm", "???"),
    entry("Wh/V", "1 Wh/V"),
    entry("m3/min", "1 m³/min"),
    entry("m3/sec", "1 m³/s"),
    entry("m3/k*liter", "1 m³/kl"), //What is this unit?!
    //    entry("nm3", "???"),
    entry("m3/A", "1 m³/A"),
    //entry("Wh hour(s)", "???"),
    //entry("m3 hour(s)", "???"),
    //entry("Wh minute(s)", "???"),
    //entry("m3 minute(s)", "???"),
    entry("%", "1 %")
  ));

  @Test
  public void toMeasurementUnit_AllKnownUnits() {
    SoftAssertions.assertSoftly(softly ->
      meteringUnits.forEach((unit, expected) -> {
        MeasurementUnit result = converter.convert(
          MeasurementUnit.of(unit, 1),
          unit
        );
        softly.assertThat(result.toString()).isEqualTo(expected);
      })
    );
  }

  @Test
  public void toMeasurementUnit_ToWrongDimension() {
    assertThatThrownBy(() -> converter.convert(MeasurementUnit.from("1 ㎥"), "㎥/h"))
      .isInstanceOf(UnitConversionError.class)
      .hasMessageContaining("Can not convert");
  }

  @Test
  public void isSameDimension_Allows_SameUnit() {
    assertThat(converter.isSameDimension("K", "K")).isTrue();
  }

  @Test
  public void isSameDimension_Allows_DifferentUnitsSameDimension() {
    assertThat(converter.isSameDimension("K", "°C")).isTrue();
  }

  @Test
  public void isSameDimension_Allows_SameUnitDifferentPrefix() {
    assertThat(converter.isSameDimension("Wh", "MWh")).isTrue();
  }

  @Test
  public void isSameDimenssion_Allows_DifferentUnitsDifferentPrefixSameDimension() {
    assertThat(converter.isSameDimension("kK", "°C")).isTrue();
  }

  @Test
  public void isSameDimension_Disallows_ValuesIncluded() {
    assertThat(converter.isSameDimension("1 K", "1 K")).isFalse();
  }

  @Test
  public void isSameDimension_Disallows_UnitsFromDifferentDimensions() {
    assertThat(converter.isSameDimension("h", "m")).isFalse();
  }

  @Test
  public void isSameDimension_Disallows_NonExistingUnits_Matching() {
    assertThat(converter.isSameDimension("lalalala", "lalalala")).isFalse();
  }

  @Test
  public void isSameDimension_Disallows_NonExistingUnits_NotMatching() {
    assertThat(converter.isSameDimension("lalalala", "dadadada")).isFalse();
  }
}
