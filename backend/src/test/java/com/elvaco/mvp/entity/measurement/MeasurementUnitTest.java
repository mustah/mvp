package com.elvaco.mvp.entity.measurement;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementUnitTest {

  @Test
  public void formatsValuesCorrectly() {
    assertThat(new MeasurementUnit("m", 12.0).toString()).isEqualTo("12 m");
    assertThat(new MeasurementUnit("m", 14.5).toString()).isEqualTo("14.5 m");
    assertThat(new MeasurementUnit("m", 14.1234).toString()).isEqualTo("14.1234 m");
  }
}
