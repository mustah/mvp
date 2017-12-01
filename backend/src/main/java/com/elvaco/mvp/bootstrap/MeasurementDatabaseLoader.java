package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import java.util.Date;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class MeasurementDatabaseLoader implements CommandLineRunner {

  private final MeasurementRepository repository;
  private final PhysicalMeterRepository meterRepository;

  @Autowired
  public MeasurementDatabaseLoader(MeasurementRepository repository, PhysicalMeterRepository
      meterRepository) {
    this.repository = repository;
    this.meterRepository = meterRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    PhysicalMeterEntity meter = meterRepository.findAll().iterator().next();
    if (meter == null) {
      throw new RuntimeException("No physical meter found!");
    }
    Stream.of(
        new MeasurementEntity(
            new Date(),
            "Butter temperature",
            12.44,
            "°C",
            meter)
    ).forEach(repository::save);
  }
}
