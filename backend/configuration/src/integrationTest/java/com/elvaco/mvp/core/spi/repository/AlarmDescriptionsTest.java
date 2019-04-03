package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AlarmDescriptionsTest extends IntegrationTest {
  @Autowired
  AlarmDescriptions alarmDescriptions;

  @Test
  public void noDescriptionFound() {
    assertThat(
      alarmDescriptions.descriptionFor("NOT_A_MANUFACTURER", 99999, 99999, 2048)
    ).isEmpty();
  }

  @Test
  public void invalidMasksThrowsException() {
    assertThatThrownBy(() ->
      alarmDescriptions.descriptionFor("ELV", 1, 1, 3)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void nullParametersReturnsEmpty() {
    assertThat(alarmDescriptions.descriptionFor("ELV", 1, null, 1)).isEmpty();
    assertThat(alarmDescriptions.descriptionFor("ELV", null, 1, 1)).isEmpty();
    assertThat(alarmDescriptions.descriptionFor(null, 1, null, 1)).isEmpty();
  }

  @Test
  public void descriptionFound() {
    assertThat(
      alarmDescriptions.descriptionFor(
        "ELR", 7, 1, 1
      )
    ).hasValue("Meter battery low");
  }
}
