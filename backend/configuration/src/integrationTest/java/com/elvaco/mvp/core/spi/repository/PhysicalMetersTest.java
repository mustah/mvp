package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class PhysicalMetersTest extends IntegrationTest {

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @After
  public void tearDown() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void createNew() {
    UUID id = randomUUID();
    PhysicalMeter saved = physicalMeters.save(new PhysicalMeter(
      id,
      "someId",
      "an-external-id",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));

    assertThat(saved.id).isEqualTo(id);
  }

  @Test
  public void update() {
    PhysicalMeter saved = physicalMeters.save(
      new PhysicalMeter(
        randomUUID(),
        "something-else",
        "an-external-id",
        "unknown",
        "ELV",
        context().organisation(),
        15
      ));

    assertThat(saved.medium).isEqualTo("unknown");

    PhysicalMeter updated = physicalMeters.save(new PhysicalMeter(
      saved.id,
      saved.organisation,
      saved.address,
      "an-external-id",
      "Heat",
      "ELV",
      15
    ));

    assertThat(updated.id).isEqualTo(saved.id);
    assertThat(updated.medium).isEqualTo("Heat");
  }

  @Test
  public void findAll() {
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "test12",
      "external-id-1",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "test13",
      "external-id-2",
      "Vacuum",
      "ELV",
      context().organisation(),
      15
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "test14",
      "external-id-3",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));

    assertThat(physicalMeters.findAll()).hasSize(3);
  }

  @Test
  public void findByOrganisationAndExternalIdAndAddress() {
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "123456789",
      "12",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));

    assertThat(
      physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
        context().getOrganisationId(),
        "12",
        "123456789"
      )
        .isPresent()).isTrue();
  }

  @Test
  public void findByMedium() {
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "abc123",
      "AAA",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "cvb123",
      "BBB",
      "Vacuum",
      "ELV",
      context().organisation(),
      15
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "oiu876",
      "CCC",
      "Heat",
      "ELV",
      context().organisation(),
      15
    ));

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }

  @Test
  public void savingMeterSavesLogs() {
    UUID meterId = randomUUID();
    ZonedDateTime start = ZonedDateTime.now();

    physicalMeters.save(
      new PhysicalMeter(
        meterId,
        context().organisation(),
        "address",
        "external-id",
        "",
        "",
        null,
        0,
        0L,
        singletonList(new StatusLogEntry<>(
            meterId,
            StatusType.ERROR,
            start
          )
        )
      )
    );

    PhysicalMeter found = physicalMeters.findAll().get(0);

    assertThat(found.statuses).hasSize(1);
    assertThat(found.statuses.get(0).start).isEqualTo(start);
    assertThat(found.statuses.get(0).status).isEqualTo(StatusType.ERROR);
  }
}
