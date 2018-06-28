package com.elvaco.mvp.database.entity.measurement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeasurementUnit implements Serializable {

  private static final long serialVersionUID = -8237162225719810274L;
  private static final Pattern UNIT_EXPONENT_RE = Pattern.compile("(.*)\\^([\\d]+)");
  private static final Map<String, String> DIGIT_TO_EXPONENT_MAP = new HashMap<>();
  private String unit;
  private double value;

  public MeasurementUnit() {}

  public MeasurementUnit(@Nullable String unit, double value) {
    if (unit == null || unit.trim().isEmpty()) {
      throw new IllegalArgumentException(String.format("Empty unit for value '%f'", value));
    }
    this.unit = superscriptExponent(unit);
    this.value = value;
  }

  public static MeasurementUnit from(String valueUnit) {
    int i = valueUnit.lastIndexOf(' ');
    if (i < 0) {
      throw new IllegalArgumentException(String.format("Missing unit in '%s'", valueUnit));
    }
    String[] parts = {valueUnit.substring(0, i), valueUnit.substring(i + 1)};
    double value;
    try {
      value = Double.parseDouble(parts[0]);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(String.format("Not a number: %s", valueUnit));
    }
    return new MeasurementUnit(parts[1], value);
  }

  public double getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }

  @Override
  public String toString() {
    if (value == (long) value) {
      return String.format("%d %s", (long) value, unit);
    } else {
      return String.format("%s %s", value, unit);
    }
  }

  private String superscriptExponent(String unit) {
    Matcher matcher = UNIT_EXPONENT_RE.matcher(unit);
    if (!matcher.matches()) {
      return unit;
    }
    String exponentString = matcher.group(2);
    StringBuilder superscriptedExponent = new StringBuilder();
    for (int i = 0; i < exponentString.length(); i++) {
      String character = String.valueOf(exponentString.charAt(i));
      superscriptedExponent.append(
        DIGIT_TO_EXPONENT_MAP.getOrDefault(character, character)
      );
    }
    return matcher.group(1) + superscriptedExponent.toString();
  }

  static {
    DIGIT_TO_EXPONENT_MAP.put("0", "⁰");
    DIGIT_TO_EXPONENT_MAP.put("1", "¹");
    DIGIT_TO_EXPONENT_MAP.put("2", "²");
    DIGIT_TO_EXPONENT_MAP.put("3", "³");
    DIGIT_TO_EXPONENT_MAP.put("4", "⁴");
    DIGIT_TO_EXPONENT_MAP.put("5", "⁵");
    DIGIT_TO_EXPONENT_MAP.put("6", "⁶");
    DIGIT_TO_EXPONENT_MAP.put("7", "⁷");
    DIGIT_TO_EXPONENT_MAP.put("8", "⁸");
    DIGIT_TO_EXPONENT_MAP.put("9", "⁹");
  }
}
