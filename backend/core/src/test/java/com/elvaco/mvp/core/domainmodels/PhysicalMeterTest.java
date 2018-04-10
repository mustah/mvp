package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterTest {

  @Test
  public void firstStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter meter = newPhysicalMeterWithStatuses(emptyList());

    List<MeterStatusLog> statuses = meter.replaceActiveStatus(StatusType.OK, now).statuses;

    assertThat(statuses).containsExactly(
      new MeterStatusLog(null, meter.id, StatusType.OK, now, null)
    );
  }

  @Test
  public void replacesDifferentStatus() {
    UUID meterId = randomUUID();
    MeterStatusLog previousStatus = new MeterStatusLog(
      null,
      meterId,
      StatusType.OK,
      ZonedDateTime.now(),
      null
    );
    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(
      previousStatus
    ));

    ZonedDateTime now = ZonedDateTime.now();
    List<MeterStatusLog> statuses = meter.replaceActiveStatus(StatusType.ERROR, now).statuses;

    assertThat(statuses).containsExactly(
      previousStatus.withStop(now),
      new MeterStatusLog(null, meterId, StatusType.ERROR, now, null)
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    UUID meterId = randomUUID();
    MeterStatusLog previousStatus = new MeterStatusLog(
      null,
      meterId,
      StatusType.OK,
      ZonedDateTime.now(),
      null
    );
    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(
      previousStatus
    ));

    List<MeterStatusLog> statuses = meter.replaceActiveStatus(
      StatusType.OK,
      ZonedDateTime.now()
    ).statuses;

    assertThat(statuses).containsExactly(
      previousStatus
    );
  }

  private PhysicalMeter newPhysicalMeterWithStatuses(
    UUID meterId,
    List<MeterStatusLog> statusLogs
  ) {
    return new PhysicalMeter(
      meterId,
      new Organisation(randomUUID(), "an-organisation", "an-organisation"),
      "12341234",
      "an-external-id",
      "Hot water",
      "ELV",
      randomUUID(),
      0L,
      0L,
      statusLogs
    );
  }

  private PhysicalMeter newPhysicalMeterWithStatuses(
    List<MeterStatusLog> statusLogs
  ) {
    return newPhysicalMeterWithStatuses(randomUUID(), statusLogs);

  }
}
