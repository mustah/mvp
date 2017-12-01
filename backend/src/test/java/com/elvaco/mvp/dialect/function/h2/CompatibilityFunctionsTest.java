package com.elvaco.mvp.dialect.function.h2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompatibilityFunctionsTest {
  @Test
  public void convertDegreeScale() throws Exception {
    assertEquals("287.15 K", CompatibilityFunctions.unitAt("14 Celsius", "K"));
  }


  // Verify that the aliases that we've defined for PostgreSql also work here
  @Test
  public void unitAliasForCelsius() throws Exception {
    assertEquals("14 ℃", CompatibilityFunctions.unitAt("14 Celsius", "℃"));
  }

  @Test
  public void unitAliasForKelvin() throws Exception {
    assertEquals("287.15 K", CompatibilityFunctions.unitAt("287.15 Kelvin", "K"));
  }

  @Test
  public void unitAliasForCubicMeters() throws Exception {
    assertEquals("43 ㎥", CompatibilityFunctions.unitAt("43 m^3", "㎥"));
  }

  @Test
  public void unitAliasForCubicMetersNoCaret() throws Exception {
    assertEquals("43 ㎥", CompatibilityFunctions.unitAt("43 m3", "㎥"));
  }
}
