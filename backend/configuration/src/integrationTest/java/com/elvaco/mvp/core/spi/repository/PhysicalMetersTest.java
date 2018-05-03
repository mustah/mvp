package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
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

  @Test
  public void findByOrganisationAndExternalIdAndAddress() {
    physicalMeters.save(physicalMeter()
      .externalId("12")
      .address("123456789")
      .build());

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
  public void savingMeterSavesLogs() {
    UUID meterId = randomUUID();
    ZonedDateTime start = ZonedDateTime.now();

    physicalMeters.save(physicalMeter()
      .id(meterId)
      .statuses(
        singletonList(new StatusLogEntry<>(
          meterId,
          StatusType.ERROR,
          start
        ))
      )
      .build());

    PhysicalMeter found = physicalMeters.findAll().get(0);

    assertThat(found.statuses).hasSize(1);
    assertThat(found.statuses.get(0).start).isEqualTo(start);
    assertThat(found.statuses.get(0).status).isEqualTo(StatusType.ERROR);
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
