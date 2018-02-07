package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.fixture.DomainModels.ELVACO;
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
    PhysicalMeter physicalMeter = new PhysicalMeter(null, ELVACO, "someId", "Heat");

    PhysicalMeter saved = physicalMeters.save(physicalMeter);

    assertThat(saved.id).isPositive();
  }

  @Test
  public void update() {
    PhysicalMeter physicalMeter = new PhysicalMeter(null, ELVACO, "something-else", "unknown");

    PhysicalMeter saved = physicalMeters.save(physicalMeter);

    assertThat(saved.medium).isEqualTo("unknown");

    PhysicalMeter updated = physicalMeters.save(new PhysicalMeter(
      saved.id,
      saved.organisation,
      saved.identity,
      "Heat"
    ));

    assertThat(updated.id).isEqualTo(saved.id);
    assertThat(updated.medium).isEqualTo("Heat");
  }

  @Test
  public void findAll() {
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test12", "Heat"));
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test13", "Vacuum"));
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test14", "Heat"));

    assertThat(physicalMeters.findAll()).hasSize(3);
  }

  @Test
  public void findByIdentity() {
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "myId", "Heat"));

    assertThat(physicalMeters.findByIdentity("myId").isPresent()).isTrue();
  }

  @Test
  public void findByMedium() {
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test12", "Heat"));
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test13", "Vacuum"));
    physicalMeters.save(new PhysicalMeter(null, ELVACO, "test14", "Heat"));

    assertThat(physicalMeters.findByMedium("Heat")).hasSize(2);
  }
}
