package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
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
  private PhysicalMeters physicalMeters;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Before
  public void setUp() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
  }

  @After
  public void tearDown() {
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
    UUID meterId = randomUUID();

    StatusLogEntry<UUID> activeStatus = StatusLogEntry.<UUID>builder()
      .id(1L)
      .entityId(meterId)
      .status(OK)
      .start(ZonedDateTime.now())
      .build();

    physicalMeters.save(physicalMeter()
      .id(meterId)
      .externalId("12")
      .address("123456789")
      .build());

    meterStatusLogs.save(activeStatus);

    PhysicalMeter physicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      context().organisationId(),
      "12",
      "123456789"
    ).get();

    List<PhysicalMeterStatusLogEntity> all = physicalMeterStatusLogJpaRepository.findAll();
    assertThat(all)
      .extracting("physicalMeterId")
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
    UUID meterId = randomUUID();
    ZonedDateTime start = ZonedDateTime.now();

    physicalMeters.save(
      physicalMeter()
        .id(meterId)
        .status(StatusLogEntry.<UUID>builder()
          .entityId(meterId)
          .status(ERROR)
          .start(start)
          .build())
        .build());

    physicalMeterJpaRepository.delete(meterId);

    assertThat(physicalMeterStatusLogJpaRepository.findAll()).isEmpty();
  }

  @Test
  public void meterShouldHaveTwoStatusLogs() {
    UUID meterId = randomUUID();
    ZonedDateTime start = ZonedDateTime.now();

    physicalMeters.save(physicalMeter().id(meterId).build());

    meterStatusLogs.save(asList(
      StatusLogEntry.<UUID>builder()
        .entityId(meterId)
        .status(OK)
        .start(start.minusDays(2))
        .build(),
      StatusLogEntry.<UUID>builder()
        .entityId(meterId)
        .status(ERROR)
        .start(start)
        .build()
    ));

    assertThat(physicalMeterStatusLogJpaRepository.findAll())
      .filteredOn("physicalMeterId", meterId)
      .hasSize(2);
  }

  @Test
  public void cannotSaveMeterWithSameStatusLog() {
    UUID meterId = randomUUID();

    physicalMeters.save(physicalMeter().id(meterId).build());

    StatusLogEntry<UUID> status = StatusLogEntry.<UUID>builder()
      .entityId(meterId)
      .status(OK)
      .start(ZonedDateTime.now())
      .build();

    meterStatusLogs.save(status);

    assertThatThrownBy(() -> meterStatusLogs.save(status))
      .isInstanceOf(DataIntegrityViolationException.class);

    assertThat(physicalMeterStatusLogJpaRepository.findAll()).hasSize(1);
  }

  private PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("someId")
      .externalId("an-external-id")
      .medium("Heat")
      .manufacturer("ELV")
      .readIntervalMinutes(15);
  }
}
