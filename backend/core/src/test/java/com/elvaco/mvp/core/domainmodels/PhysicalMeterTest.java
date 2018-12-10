package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterTest {

  @Test
  public void firstStatus() {
    var now = ZonedDateTime.now();
    var meter = physicalMeter().build();

    List<StatusLogEntry> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactly(StatusLogEntry.builder()
      .primaryKey(meter.primaryKey())
      .status(OK)
      .start(now)
      .build());
  }

  @Test
  public void replacesDifferentStatus() {
    var now = ZonedDateTime.now();
    var meterId = randomUUID();
    var primaryKey = new Pk(meterId, OTHER_ORGANISATION.id);
    StatusLogEntry previousStatus = StatusLogEntry.builder()
      .primaryKey(primaryKey)
      .start(now)
      .status(OK)
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry> statuses = meter.replaceActiveStatus(ERROR, now).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(
      previousStatus.toBuilder().stop(now).build(),
      StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .start(now)
        .status(ERROR)
        .build()
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    var meterId = randomUUID();
    var now = ZonedDateTime.now();
    var primaryKey = new Pk(meterId, OTHER_ORGANISATION.id);

    StatusLogEntry previousStatus = StatusLogEntry.builder()
      .primaryKey(primaryKey)
      .start(now)
      .status(OK)
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry> statuses = meter.replaceActiveStatus(OK, now).statuses;

    assertThat(statuses).containsExactlyInAnyOrder(previousStatus);
  }

  @Test
  public void doesNotReplaceSameStatusWithDifferentTimestamps() {
    var meterId = randomUUID();
    var now = ZonedDateTime.now();
    var primaryKey = new Pk(meterId, OTHER_ORGANISATION.id);

    StatusLogEntry previousStatus = StatusLogEntry.builder()
      .primaryKey(primaryKey)
      .status(OK)
      .start(now.minusHours(1))
      .build();
    PhysicalMeter meter = physicalMeter()
      .id(meterId)
      .status(previousStatus)
      .build();

    List<StatusLogEntry> statuses = meter.replaceActiveStatus(OK, now).statuses;

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
