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

    PhysicalMeter meter = newPhysicalMeterWithStatuses(randomUUID(), emptyList());

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(StatusType.OK, now).statuses;

    assertThat(statuses).containsExactly(new StatusLogEntry<>(meter.id, StatusType.OK, now));
  }

  @Test
  public void replacesDifferentStatus() {
    UUID meterId = randomUUID();
    StatusLogEntry<UUID> previousStatus = new StatusLogEntry<>(
      meterId,
      StatusType.OK,
      ZonedDateTime.now()
    );
    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(previousStatus));

    ZonedDateTime now = ZonedDateTime.now();
    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(StatusType.ERROR, now).statuses;

    assertThat(statuses).containsExactly(
      previousStatus.withStop(now),
      new StatusLogEntry<>(meterId, StatusType.ERROR, now)
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    UUID meterId = randomUUID();
    StatusLogEntry<UUID> previousStatus = new StatusLogEntry<>(
      meterId,
      StatusType.OK,
      ZonedDateTime.now()
    );
    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(previousStatus));

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(
      StatusType.OK,
      ZonedDateTime.now()
    ).statuses;

    assertThat(statuses).containsExactly(previousStatus);
  }

  private PhysicalMeter newPhysicalMeterWithStatuses(
    UUID meterId,
    List<StatusLogEntry<UUID>> statusLogs
  ) {
    return PhysicalMeter.builder()
      .id(meterId)
      .organisation(new Organisation(
        randomUUID(),
        "an-organisation",
        "an-organisation",
        "an-organisation"
      ))
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer("ELV")
      .logicalMeterId(randomUUID())
      .statuses(statusLogs)
      .build();
  }
}
