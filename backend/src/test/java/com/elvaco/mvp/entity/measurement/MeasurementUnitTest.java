package com.elvaco.mvp.entity.measurement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MeasurementUnitTest {
  @Test
  public void formatsValuesCorrectly() throws Exception {
    assertEquals("12 m", new MeasurementUnit("m", 12.0).toString());
    assertEquals("14.5 m", new MeasurementUnit("m", 14.5).toString());
    assertEquals("14.1234 m", new MeasurementUnit("m", 14.1234).toString());
  }

}
