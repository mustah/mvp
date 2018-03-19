package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(5)
@Profile("demo")
@Component
public class MeasurementDatabaseLoader implements CommandLineRunner {

  private final PhysicalMeters physicalMeters;
  private final Measurements measurements;
  private final Random random = new Random();

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  public MeasurementDatabaseLoader(
    PhysicalMeters physicalMeters,
    Measurements measurements
  ) {
    this.physicalMeters = physicalMeters;
    this.measurements = measurements;
  }

  @Override
  public void run(String... args) {
    int measurementCount = measurements.findAll(new RequestParametersAdapter()).size();

    if (measurementCount != 0) {
      log.info("Demo measurements seems to already be loaded - skipping!");
      return;
    }
    createMeasurementMockData();
  }

  private void createMeasurementMockData() {
    MeasurementUnit measurementUnit = new MeasurementUnit("m3", 2.0);
    List<PhysicalMeter> meters = physicalMeters.findAll();

    for (int x = 0; x < meters.size(); x++) {
      boolean isFailing = false;
      if (random.nextInt(10) == 9) {
        isFailing = true;
      }
      measurementJpaRepository.save(createMeasurements(
        meters.get(x),
        measurementUnit,
        ZonedDateTime.now().minusMinutes(31 * 1440),
        meters.get(x).readIntervalMinutes,
        31 * 1440 / meters.get(x).readIntervalMinutes,
        isFailing
      ));
      log.info("Saved demo measurements " + x + "/" + meters.size());
    }
  }

  /**
   * Creates a list of fake measurements.
   *
   * @param physicalMeter   Physical meter
   * @param measurementUnit Unit of measurement
   * @param interval        Time in minutes between measurements
   * @param values          Nr of values to generate
   *
   * @return
   */
  private List<MeasurementEntity> createMeasurements(
    PhysicalMeter physicalMeter,
    MeasurementUnit measurementUnit,
    ZonedDateTime measurementDate,
    long interval,
    long values,
    boolean isFailing
  ) {
    List<MeasurementEntity> measurementEntities = new ArrayList<>();

    PhysicalMeterEntity meter = new PhysicalMeterEntity();
    meter.id = physicalMeter.id;

    for (int x = 0; x < values; x++) {
      if (isFailing && random.nextInt(10) == 9) {
        continue;
      }
      measurementEntities.add(new MeasurementEntity(
        null,
        measurementDate.plusMinutes(x * interval),
        String.valueOf(x),
        measurementUnit,
        meter
      ));
    }

    return measurementEntities;
  }
}
