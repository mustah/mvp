package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhysicalMetersTest extends IntegrationTest {

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Before
  public void setUp() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void createNew() {
    UUID id = randomUUID();

    PhysicalMeter saved = physicalMeters.save(physicalMeter().id(id).build());

    assertThat(saved.id).isEqualTo(id);
  }

  @Test
  public void updateMedium() {
    PhysicalMeterBuilder physicalMeter = physicalMeter().medium("unknown");

    PhysicalMeter saved = physicalMeters.save(physicalMeter.build());

    assertThat(saved.medium).isEqualTo("unknown");

    PhysicalMeter updated = physicalMeters.save(
      physicalMeter
        .id(saved.id)
        .medium("Heat")
        .build()
    );

    assertThat(updated.id).isEqualTo(saved.id);
    assertThat(updated.medium).isEqualTo("Heat");
  }

  @Transactional
  @Test
  public void findAll() {
    physicalMeters.save(physicalMeter()
      .address("test12")
      .externalId("external-id-1")
      .medium("Heat")
      .build());
    physicalMeters.save(physicalMeter()
      .address("test13")
      .externalId("external-id-2")
      .medium("Vacuum")
      .build());
    physicalMeters.save(physicalMeter()
      .address("test14")
      .externalId("external-id-3")
      .medium("Heat")
      .build());

    assertThat(physicalMeters.findAll()).hasSize(3);
  }

  @Transactional
  @Test
  public void findByOrganisationAndExternalIdAndAddress() {
    var meterId = randomUUID();
    var primaryKey = new Pk(meterId, context().organisationId());

    StatusLogEntry activeStatus = StatusLogEntry.builder()
      .id(1L)
      .primaryKey(primaryKey)
      .status(OK)
      .build();

    physicalMeters.save(physicalMeter()
      .id(meterId)
      .externalId("12")
      .address("123456789")
      .build());

    meterStatusLogs.save(activeStatus);

    PhysicalMeter physicalMeter = physicalMeters.findByWithStatuses(
      context().organisationId(),
      "12",
      "123456789"
    ).get();

    assertThat(physicalMeterStatusLogJpaRepository.findAll())
      .extracting("pk.physicalMeterId")
      .containsExactly(meterId);

    assertThat(physicalMeter.id).isEqualTo(meterId);
  }

  @Transactional
  @Test
  public void findByMedium() {
    physicalMeters.save(physicalMeter()
      .address("abc123")
      .externalId("AAA")
      .medium("Heat")
      .build());
    physicalMeters.save(physicalMeter()
      .address("cvb123")
      .externalId("BBB")
      .medium("Vacuum")
      .build());
    physicalMeters.save(
      physicalMeter()
        .address("oiu876")
        .externalId("CCC")
        .medium("Heat")
        .build());

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }

  @Test
  public void statusLogShouldBeRemovedWhenItsPhysicalMeterIsRemoved() {
    var start = ZonedDateTime.now();
    var meterId = randomUUID();
    var primaryKey = new Pk(meterId, context().organisationId());

    physicalMeters.save(
      physicalMeter()
        .id(meterId)
        .status(StatusLogEntry.builder()
          .primaryKey(primaryKey)
          .status(ERROR)
          .start(start)
          .build())
        .build());

    physicalMeterJpaRepository.deleteById(meterId);

    assertThat(physicalMeterStatusLogJpaRepository.findAll()).isEmpty();
  }

  @Test
  public void meterShouldHaveTwoStatusLogs() {
    var start = ZonedDateTime.now();
    var meterId = randomUUID();
    var primaryKey = new Pk(meterId, context().organisationId());

    physicalMeters.save(physicalMeter().id(meterId).build());

    meterStatusLogs.save(asList(
      StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(OK)
        .start(start.minusDays(2))
        .build(),
      StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(ERROR)
        .start(start)
        .build()
    ));

    assertThat(physicalMeterStatusLogJpaRepository.findAll())
      .filteredOn("pk.physicalMeterId", meterId)
      .hasSize(2);
  }

  @Test
  public void cannotSaveMeterWithSameStatusLog() {
    var meterId = randomUUID();
    var primaryKey = new Pk(meterId, context().organisationId());

    physicalMeters.save(physicalMeter().id(meterId).build());

    StatusLogEntry status = StatusLogEntry.builder()
      .primaryKey(primaryKey)
      .status(OK)
      .build();

    meterStatusLogs.save(status);

    assertThatThrownBy(() -> meterStatusLogs.save(status))
      .isInstanceOf(DataIntegrityViolationException.class);

    assertThat(physicalMeterStatusLogJpaRepository.findAll()).hasSize(1);
  }

  private PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisationId(context().organisationId())
      .address("someId")
      .externalId("an-external-id")
      .medium("Heat")
      .manufacturer("ELV")
      .readIntervalMinutes(15);
  }
}
