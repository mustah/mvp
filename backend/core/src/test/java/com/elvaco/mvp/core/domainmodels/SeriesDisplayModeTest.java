package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesDisplayModeTest {

  @Test
  public void toStringOfName() {
    assertThat(SeriesDisplayMode.UNKNOWN.toString()).isEqualTo("unknown");
    assertThat(SeriesDisplayMode.CONSUMPTION.toString()).isEqualTo("consumption");
    assertThat(SeriesDisplayMode.READOUT.toString()).isEqualTo("readout");
  }
}
