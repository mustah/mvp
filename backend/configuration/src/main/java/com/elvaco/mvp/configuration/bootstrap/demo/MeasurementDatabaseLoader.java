package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Order(5)
@Profile("demo")
@Component
public class MeasurementDatabaseLoader implements CommandLineRunner {

  private static final int DAYS_TO_ADD = 10;
  private final PhysicalMeters physicalMeters;
  private final SettingUseCases settingUseCases;
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  public MeasurementDatabaseLoader(
    PhysicalMeters physicalMeters,
    SettingUseCases settingUseCases
  ) {
    this.physicalMeters = physicalMeters;
    this.settingUseCases = settingUseCases;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (settingUseCases.isDemoMeasurementsLoaded()) {
      log.info("Demo measurements seems to already be loaded - skipping!");
      return;
    }
    createMeasurementMockData();

    settingUseCases.setDemoMeasurementsLoaded();
  }

  private void createMeasurementMockData() {
    List<PhysicalMeter> meters = physicalMeters.findAll();

    for (int x = 0; x < meters.size(); x++) {
      boolean isFailing = false;
      if (random.nextInt(10) >= 8) {
        isFailing = true;
      }

      ZonedDateTime startDate = ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId())
        .truncatedTo(ChronoUnit.DAYS)
        .minusDays(DAYS_TO_ADD);

      measurementJpaRepository.save(createMeasurements(
        meters.get(x),
        startDate,
        meters.get(x).readIntervalMinutes,
        DAYS_TO_ADD * 1440 / meters.get(x).readIntervalMinutes,
        isFailing
      ));
      log.info("Saved demo measurements " + x + "/" + meters.size());
    }
  }

  /**
   * Creates a list of fake measurements.
   *
   * @param physicalMeter Physical meter
   * @param interval      Time in minutes between measurements
   * @param values        Nr of values to generate
   *
   * @return
   */
  private List<MeasurementEntity> createMeasurements(
    PhysicalMeter physicalMeter,
    ZonedDateTime measurementDate,
    long interval,
    long values,
    boolean isFailing
  ) {
    List<MeasurementEntity> measurementEntities = new ArrayList<>();

    PhysicalMeterEntity meter = new PhysicalMeterEntity();
    meter.id = physicalMeter.id;

    for (int x = 0; x < values; x++) {
      if (isFailing && random.nextInt(10) >= 8) {
        continue;
      }

      ZonedDateTime created = measurementDate.plusMinutes(x * interval);

      measurementEntities.addAll(DemoDataHelper.getDistrictHeatingMeterReading(created, meter));
    }

    return measurementEntities;
  }
}
