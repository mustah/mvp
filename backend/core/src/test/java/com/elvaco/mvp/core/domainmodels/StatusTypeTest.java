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
    assertThat(StatusType.from("ok")).isEqualTo(StatusType.OK);
    assertThat(StatusType.from("warning")).isEqualTo(StatusType.WARNING);
    assertThat(StatusType.from("error")).isEqualTo(StatusType.ERROR);
    assertThat(StatusType.from("unknown")).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void ignoresCase() {
    assertThat(StatusType.from("Ok")).isEqualTo(StatusType.OK);
    assertThat(StatusType.from("OK")).isEqualTo(StatusType.OK);
    assertThat(StatusType.from("warniNG")).isEqualTo(StatusType.WARNING);
  }

  @Test
  public void mapsErrorReportedToError() {
    assertThat(StatusType.from("ErrorReported")).isEqualTo(StatusType.ERROR);
  }

  @Test
  public void canHandleNull() {
    assertThat(StatusType.from(null)).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void emptyStatus() {
    assertThat(StatusType.from("")).isEqualTo(StatusType.UNKNOWN);
  }

  @Test
  public void isReported() {
    assertThat(StatusType.isReported("error")).isTrue();
    assertThat(StatusType.isReported("ErrorReported")).isTrue();
  }

  @Test
  public void isNotReported() {
    assertThat(StatusType.isReported(null)).isFalse();
    assertThat(StatusType.isReported("unknown")).isFalse();
    assertThat(StatusType.isReported("xyz")).isFalse();
    assertThat(StatusType.isReported("ok")).isFalse();
  }
}
