package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesDisplayModeTest {

  @Test
  public void toStringOfName() {
    assertThat(DisplayMode.UNKNOWN.toString()).isEqualTo("unknown");
    assertThat(DisplayMode.CONSUMPTION.toString()).isEqualTo("consumption");
    assertThat(DisplayMode.READOUT.toString()).isEqualTo("readout");
  }
}
