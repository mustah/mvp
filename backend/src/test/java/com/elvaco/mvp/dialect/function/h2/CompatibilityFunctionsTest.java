package com.elvaco.mvp.dialect.function.h2;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompatibilityFunctionsTest {

  @Test
  public void convertDegreeScale() {
    assertThat(CompatibilityFunctions.unitAt("14 Celsius", "K")).isEqualTo("287.15 K");
  }

  // Verify that the aliases that we've defined for PostgreSql also work here
  @Test
  public void unitAliasForCelsius() {
    assertThat(CompatibilityFunctions.unitAt("14 Celsius", "℃")).isEqualTo("14 ℃");
  }

  @Test
  public void unitAliasForKelvin() {
    assertThat(CompatibilityFunctions.unitAt("287.15 Kelvin", "K")).isEqualTo("287.15 K");
  }

  @Test
  public void unitAliasForCubicMeters() {
    assertThat(CompatibilityFunctions.unitAt("43 m^3", "㎥")).isEqualTo("43 ㎥");
  }

  @Test
  public void unitAliasForCubicMetersNoCaret() {
    assertThat(CompatibilityFunctions.unitAt("43 m3", "㎥")).isEqualTo("43 ㎥");
  }
}
