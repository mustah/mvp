package com.elvaco.mvp.database.dialect.function.h2;

import org.junit.Test;

import static com.elvaco.mvp.database.dialect.function.h2.CompatibilityFunctions.toMeasurementUnit;
import static com.elvaco.mvp.database.dialect.function.h2.CompatibilityFunctions.unitAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CompatibilityFunctionsTest {
  @Test
  public void convertDegreeScale() {
    assertEquals("287.15 K", unitAt("14 Celsius", "K"));
  }

  // Verify that the aliases that we've defined for PostgreSql also work here
  @Test
  public void unitAliasForCelsius() {
    assertEquals("14 ℃", unitAt("14 Celsius", "℃"));
  }

  @Test
  public void unitAliasForKelvin() {
    assertEquals("287.15 K", unitAt("287.15 Kelvin", "K"));
  }

  @Test
  public void unitAliasForCubicMeters() {
    assertEquals("43 ㎥", unitAt("43 m^3", "㎥"));
  }

  @Test
  public void unitAliasForCubicMetersNoCaret() {
    assertEquals("43 ㎥", unitAt("43 m3", "㎥"));
  }

  @Test
  public void measurementUnits() {
    assertThat(toMeasurementUnit("150 °C", "K").toString()).isEqualTo("423.15 K");
  }
}
