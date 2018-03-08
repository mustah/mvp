package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTest {

  @Test
  public void defaultsToUnknownStatus() {
    assertThat(Status.from("x")).isEqualTo(Status.UNKNOWN);
  }

  @Test
  public void fromStringValue() {
    assertThat(Status.from("active")).isEqualTo(Status.ACTIVE);
    assertThat(Status.from("ok")).isEqualTo(Status.OK);
    assertThat(Status.from("critical")).isEqualTo(Status.CRITICAL);
    assertThat(Status.from("warning")).isEqualTo(Status.WARNING);
  }

  @Test
  public void ignoresCase() {
    assertThat(Status.from("Active")).isEqualTo(Status.ACTIVE);
    assertThat(Status.from("Ok")).isEqualTo(Status.OK);
    assertThat(Status.from("Info")).isEqualTo(Status.INFO);
  }
}
