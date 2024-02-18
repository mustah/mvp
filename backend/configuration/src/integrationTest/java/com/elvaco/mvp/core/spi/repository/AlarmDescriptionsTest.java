package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.core.domainmodels.AlarmDescriptionQuery;
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

    assertThat(alarmDescriptions.descriptionFor(
      AlarmDescriptionQuery.builder()
        .manufacturer("NOT_A_MANUFACTURER")
        .firmwareVersion(99999)
        .deviceType(99999)
        .mask(2048)
        .build())
    ).isEmpty();
  }

  @Test
  public void invalidMasksThrowsException() {
    assertThatThrownBy(() ->
      alarmDescriptions.descriptionFor(
        AlarmDescriptionQuery.builder()
          .manufacturer("ELV")
          .firmwareVersion(1)
          .deviceType(1)
          .mask(3)
          .build()
      )
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void nullParametersReturnsEmpty() {
    var withoutFirmwareVersion = AlarmDescriptionQuery.builder()
      .manufacturer("ELV")
      .deviceType(1)
      .mask(1)
      .build();
    assertThat(alarmDescriptions.descriptionFor(withoutFirmwareVersion)).isEmpty();

    var withoutDeviceType = AlarmDescriptionQuery.builder()
      .manufacturer("ELV")
      .firmwareVersion(1)
      .mask(1)
      .build();
    assertThat(alarmDescriptions.descriptionFor(withoutDeviceType)).isEmpty();

    var withoutManufacturer = AlarmDescriptionQuery.builder()
      .firmwareVersion(1)
      .deviceType(1)
      .mask(1)
      .build();
    assertThat(alarmDescriptions.descriptionFor(withoutManufacturer)).isEmpty();
  }

  @Test
  public void descriptionFound() {
    var withLowBattery = AlarmDescriptionQuery.builder()
      .manufacturer("ELV")
      .firmwareVersion(7)
      .deviceType(1)
      .mask(1)
      .build();
    assertThat(alarmDescriptions.descriptionFor(withLowBattery)).isEqualTo("Meter battery low");
  }
}
