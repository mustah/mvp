package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTypeTest {

  @Test
  public void defaultsToUnknownStatus() {
    assertThat(StatusType.from("x")).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void fromStringValue() {
    assertThat(StatusType.from("active")).isEqualTo(StatusType.ACTIVE);
    assertThat(StatusType.from("ok")).isEqualTo(StatusType.OK);
    assertThat(StatusType.from("critical")).isEqualTo(StatusType.CRITICAL);
    assertThat(StatusType.from("warning")).isEqualTo(StatusType.WARNING);
  }

  @Test
  public void ignoresCase() {
    assertThat(StatusType.from("Active")).isEqualTo(StatusType.ACTIVE);
    assertThat(StatusType.from("Ok")).isEqualTo(StatusType.OK);
    assertThat(StatusType.from("Info")).isEqualTo(StatusType.INFO);
  }
}
