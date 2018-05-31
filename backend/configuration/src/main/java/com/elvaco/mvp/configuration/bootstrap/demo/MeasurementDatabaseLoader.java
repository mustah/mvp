package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper.electricityMeasurement;
import static com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper.gasMeasurement;
import static com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper.heatMeasurement;
import static com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper.roomSensorMeasurement;
import static com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper.waterMeasurement;

@Slf4j
@RequiredArgsConstructor
@Order(5)
@Profile("demo")
@Component
public class MeasurementDatabaseLoader implements CommandLineRunner {

  private static final int DAYS_TO_ADD = 10;

  private final PhysicalMeters physicalMeters;
  private final SettingUseCases settingUseCases;
  private final MeasurementJpaRepositoryImpl measurementJpaRepository;

  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  @Override
  @Transactional
  public void run(String... args) {
    if (settingUseCases.isDemoMeasurementsLoaded()) {
      log.info("Demo measurements seems to already be loaded - skipping!");
      return;
    }
    log.info("Creating demo measurements");
    createMeasurementMockData();

    settingUseCases.setDemoMeasurementsLoaded();
  }

  private void createMeasurementMockData() {
    List<PhysicalMeter> meters = physicalMeters.findAll();

    for (int i = 0; i < meters.size(); i++) {
      boolean isFailing = random.nextInt(10) >= 8;

      ZonedDateTime startDate = ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId())
        .truncatedTo(ChronoUnit.DAYS)
        .minusDays(DAYS_TO_ADD);

      PhysicalMeter physicalMeter = meters.get(i);
      measurementJpaRepository.save(createMeasurements(
        physicalMeter,
        startDate,
        physicalMeter.readIntervalMinutes,
        DAYS_TO_ADD * 1440 / physicalMeter.readIntervalMinutes,
        isFailing
      ));
      log.info("Saved demo measurements " + i + "/" + meters.size());
    }
  }

  private List<MeasurementEntity> createMeasurements(
    PhysicalMeter physicalMeter,
    ZonedDateTime measurementDate,
    long interval,
    long values,
    boolean isFailing
  ) {
    PhysicalMeterEntity meter = new PhysicalMeterEntity();
    meter.id = physicalMeter.id;
    meter.medium = physicalMeter.medium;

    List<MeasurementEntity> measurementEntities = new ArrayList<>();

    double consumingMediumMeterReading = 13;

    for (int i = 0; i < values; i++) {
      if (isFailing && random.nextInt(10) >= 8) {
        continue;
      }
      ZonedDateTime created = measurementDate.plusMinutes(i * interval);

      List<MeasurementEntity> measurements;
      if (meter.medium.equals(Medium.HEAT_RETURN_TEMP.medium)) {
        measurements = heatMeasurement(created, meter);
      } else if (meter.medium.equals(Medium.GAS.medium)) {
        measurements = gasMeasurement(created, meter, consumingMediumMeterReading);
      } else if (meter.medium.equals(Medium.WATER.medium)) {
        measurements = waterMeasurement(created, meter, consumingMediumMeterReading);
      } else if (meter.medium.equals(Medium.COLD_WATER.medium)) {
        measurements = waterMeasurement(created, meter, consumingMediumMeterReading);
      } else if (meter.medium.equals(Medium.HOT_WATER.medium)) {
        measurements = waterMeasurement(created, meter, consumingMediumMeterReading);
      } else if (meter.medium.equals(Medium.ELECTRICITY.medium)) {
        measurements = electricityMeasurement(created, meter, consumingMediumMeterReading);
      } else if (meter.medium.equals(Medium.ROOM_TEMP.medium)) {
        measurements = roomSensorMeasurement(created, meter);
      } else {
        throw new RuntimeException("You need to add support for mocking the medium " + meter
          .medium + " in DemoDataHelper");
      }
      consumingMediumMeterReading = measurements.get(0).value.getValue();

      measurementEntities.addAll(measurements);
    }
    return measurementEntities;
  }
}
