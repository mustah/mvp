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
        "unknown",
        "ELV"
      ));

    assertThat(saved.medium).isEqualTo("unknown");

    PhysicalMeter updated = physicalMeters.save(new PhysicalMeter(
      saved.id,
      saved.organisation,
      saved.identity,
      "Heat",
      "ELV"
    ));

    assertThat(updated.id).isEqualTo(saved.id);
    assertThat(updated.medium).isEqualTo("Heat");
  }

  @Test
  public void findAll() {
    physicalMeters.save(new PhysicalMeter(ELVACO, "test12", "Heat", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "test13", "Vacuum", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "test14", "Heat", "ELV"));

    assertThat(physicalMeters.findAll()).hasSize(3);
  }

  @Test
  public void findByIdentity() {
    physicalMeters.save(new PhysicalMeter(ELVACO, "myId", "Heat", "ELV"));

    assertThat(physicalMeters.findByIdentity("myId").isPresent()).isTrue();
  }

  @Test
  public void findByMedium() {
    physicalMeters.save(new PhysicalMeter(ELVACO, "abc123", "Heat", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "cvb123", "Vacuum", "ELV"));
    physicalMeters.save(new PhysicalMeter(ELVACO, "oiu876", "Heat", "ELV"));

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }
}
