package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterTest {

  @Test
  public void firstStatus() {
    ZonedDateTime now = ZonedDateTime.now();

    PhysicalMeter meter = newPhysicalMeterWithStatuses(randomUUID(), emptyList());

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactly(StatusLogEntry.<UUID>builder()
      .entityId(meter.id)
      .status(OK)
      .start(now)
      .build());
  }

  @Test
  public void replacesDifferentStatus() {
    UUID meterId = randomUUID();
    StatusLogEntry<UUID> previousStatus = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .status(OK)
      .build();

    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(previousStatus));

    ZonedDateTime now = ZonedDateTime.now();
    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(ERROR, now).statuses;

    assertThat(statuses).containsExactly(
      previousStatus.toBuilder().stop(now).build(),
      StatusLogEntry.<UUID>builder()
        .entityId(meterId)
        .status(ERROR)
        .build()
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    UUID meterId = randomUUID();
    StatusLogEntry<UUID> previousStatus = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .status(OK)
      .build();
    PhysicalMeter meter = newPhysicalMeterWithStatuses(meterId, singletonList(previousStatus));

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(OK).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(previousStatus);
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
