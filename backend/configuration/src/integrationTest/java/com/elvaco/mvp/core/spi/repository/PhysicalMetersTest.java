package com.elvaco.mvp.core.spi.repository;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.IntegrationTestFixtureContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMetersTest extends IntegrationTest {

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;
  private IntegrationTestFixtureContext context;

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    destroyContext(context);
  }

  @Before
  public void setUp() {
    context = newContext();
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
      context.organisation()
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
        context.organisation()
      ));

    assertThat(saved.medium).isEqualTo("unknown");

    PhysicalMeter updated = physicalMeters.save(new PhysicalMeter(
      saved.id,
      saved.organisation,
      saved.address,
      "an-external-id",
      "Heat",
      "ELV"
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
      context.organisation()
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "test13",
      "external-id-2",
      "Vacuum",
      "ELV",
      context.organisation()
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "test14",
      "external-id-3",
      "Heat",
      "ELV",
      context.organisation()
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
      context.organisation()
    ));

    assertThat(
      physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
        context.organisation().id,
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
      context.organisation()
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "cvb123",
      "BBB",
      "Vacuum",
      "ELV",
      context.organisation()
    ));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "oiu876",
      "CCC",
      "Heat",
      "ELV",
      context.organisation()
    ));

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }
}
