package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterTest {

  @Test
  public void firstStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    PhysicalMeter meter = physicalMeter().build();

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactly(StatusLogEntry.<UUID>builder()
      .entityId(meter.id)
      .status(OK)
      .start(now)
      .build());
  }

  @Test
  public void replacesDifferentStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    UUID meterId = randomUUID();
    StatusLogEntry<UUID> previousStatus = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .start(now)
      .status(OK)
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(ERROR, now).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(
      previousStatus.toBuilder().stop(now).build(),
      StatusLogEntry.<UUID>builder()
        .entityId(meterId)
        .start(now)
        .status(ERROR)
        .build()
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    UUID meterId = randomUUID();
    ZonedDateTime now = ZonedDateTime.now();
    StatusLogEntry<UUID> previousStatus = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .start(now)
      .status(OK)
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(previousStatus);
  }

  @Test
  public void doesNotReplaceSameStatusWithDifferentTimestamps() {
    UUID meterId = randomUUID();
    ZonedDateTime now = ZonedDateTime.now();
    StatusLogEntry<UUID> previousStatus = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .status(OK)
      .start(now.minusHours(1))
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry<UUID>> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(previousStatus);
  }

  private static PhysicalMeter.PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisationId(OTHER_ORGANISATION.id)
      .address("12341234")
      .externalId("an-external-id")
      .medium("Hot water")
      .manufacturer("ELV")
      .logicalMeterId(randomUUID());
  }
}
