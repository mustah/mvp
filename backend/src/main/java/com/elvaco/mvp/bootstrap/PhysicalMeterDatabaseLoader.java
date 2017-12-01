package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeteringPointRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class PhysicalMeterDatabaseLoader implements CommandLineRunner {
  private final PhysicalMeterRepository repository;
  private final MeteringPointRepository meteringPointRepository;

  @Autowired
  public PhysicalMeterDatabaseLoader(PhysicalMeterRepository repository, MeteringPointRepository
      meteringPointRepository) {
    this.repository = repository;
    this.meteringPointRepository = meteringPointRepository;
  }

  @Override
  public void run(String... args) {
    PhysicalMeterEntity physicalMeterEntity = repository.findOne(3L);
    if (physicalMeterEntity == null) {
      physicalMeterEntity = new PhysicalMeterEntity(1L /*fixme: this should be an organisation
      entity*/, "test-butter-meter-1", "Butter");
      physicalMeterEntity.setMeteringPoint(meteringPointRepository.findOne(1L));
      repository.save(physicalMeterEntity);
    }
  }
}
