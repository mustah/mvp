package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;

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

    List<StatusLogEntry> statuses = replaceActiveStatusWith(OK, meter, now);

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
    var previousStatusEntry = StatusLogEntry.builder()
      .primaryKey(primaryKey)
      .start(now)
      .status(OK)
      .build();
    var meter = physicalMeter()
      .id(meterId)
      .status(previousStatusEntry)
      .build();

    List<StatusLogEntry> statuses = replaceActiveStatusWith(ERROR, meter, now);

    assertThat(statuses).containsExactlyInAnyOrder(
      previousStatusEntry.toBuilder().stop(now).build(),
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
    var previousStatusEntry = StatusLogEntry.builder()
      .primaryKey(new Pk(meterId, OTHER_ORGANISATION.id))
      .start(now)
      .status(OK)
      .build();
    var meter = physicalMeter()
      .id(meterId)
      .status(previousStatusEntry)
      .build();

    List<StatusLogEntry> statuses = replaceActiveStatusWith(OK, meter, now);

    assertThat(statuses).containsExactlyInAnyOrder(previousStatusEntry);
  }

  @Test
  public void doesNotReplaceSameStatusWithDifferentTimestamps() {
    var meterId = randomUUID();
    var now = ZonedDateTime.now();
    var previousStatusEntry = StatusLogEntry.builder()
      .primaryKey(new Pk(meterId, OTHER_ORGANISATION.id))
      .status(OK)
      .start(now.minusHours(1))
      .build();
    var meter = physicalMeter()
      .id(meterId)
      .status(previousStatusEntry)
      .build();

    List<StatusLogEntry> statuses = replaceActiveStatusWith(OK, meter, now);

    assertThat(statuses).containsExactlyInAnyOrder(previousStatusEntry);
  }

  private static List<StatusLogEntry> replaceActiveStatusWith(
    StatusType status,
    PhysicalMeter meter,
    ZonedDateTime now
  ) {
    return StatusLogEntryHelper.replaceActiveStatus(
      List.copyOf(meter.statuses),
      StatusLogEntry.builder()
        .primaryKey(meter.primaryKey())
        .status(status)
        .start(now)
        .build()
    );
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
