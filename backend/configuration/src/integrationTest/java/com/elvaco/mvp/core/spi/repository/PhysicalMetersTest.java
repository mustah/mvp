package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMetersTest extends IntegrationTest {

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void createNew() {
    PhysicalMeter saved = physicalMeters.save(
      new PhysicalMeter(
        ELVACO,
        "someId",
        "an-external-id",
        "Heat",
        "ELV"
      ));

    assertThat(saved.id).isPositive();
  }

  @Test
  public void update() {
    PhysicalMeter saved = physicalMeters.save(
      new PhysicalMeter(
        ELVACO,
        "something-else",
        "an-external-id",
        "unknown",
        "ELV"
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
    physicalMeters.save(new PhysicalMeter(ELVACO, "test12", "external-id-1", "Heat", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "test13", "external-id-2", "Vacuum", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "test14", "external-id-3", "Heat", "ELV"));

    assertThat(physicalMeters.findAll()).hasSize(3);
  }

  @Test
  public void findByOrganisationAndExternalIdAndAddress() {
    physicalMeters.save(new PhysicalMeter(ELVACO, "123456789", "12", "Heat", "ELV"));

    assertThat(
      physicalMeters.findByOrganisationAndExternalIdAndAddress(ELVACO, "12", "123456789")
        .isPresent()).isTrue();
  }

  @Test
  public void findByMedium() {
    physicalMeters.save(new PhysicalMeter(ELVACO, "abc123", "AAA", "Heat", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "cvb123", "BBB", "Vacuum", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "oiu876", "CCC", "Heat", "ELV"));

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }
}
