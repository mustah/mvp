package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.database.fixture.Entities.ELVACO_ENTITY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Order(2)
@Profile("demo")
@Component
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final PhysicalMeterJpaRepository physicalMeterRepository;
  private final GatewayRepository gatewayRepository;
  private final SettingUseCases settingUseCases;

  @Autowired
  public DemoDataLoader(
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    MeasurementJpaRepository measurementJpaRepository,
    PhysicalMeterJpaRepository physicalMeterRepository,
    GatewayRepository gatewayRepository,
    SettingUseCases settingUseCases
  ) {
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.physicalMeterRepository = physicalMeterRepository;
    this.gatewayRepository = gatewayRepository;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) {
    if (settingUseCases.isDemoDataLoaded()) {
      log.info("Demo data seems to already be loaded - skipping demo data loading!");
      return;
    }

    Random random = new Random();

    int meters = 10;
    int daysPerMeter = 30;
    int reportIntervalInMinutes = 15;
    int measurementCount = daysPerMeter * 24 * 4; // One day, 15-minute delivery interval
    Instant firstDeliveryInstant = Instant.now().minus(daysPerMeter, ChronoUnit.DAYS);

    for (int i = 0; i < meters; ++i) {
      String meterIdentity = "DEMO-METER-" + i;
      String serial = "DEMO-GATEWAY-" + i;

      List<GatewayEntity> gatewayEntities = singletonList(mockGateway(serial));

      LogicalMeterEntity logicalMeterEntity = mockLogicalMeterEntity(
        meterIdentity,
        gatewayEntities,
        i
      );

      PhysicalMeterEntity physicalMeterEntity = mockPhysicalMeter(
        ELVACO_ENTITY,
        meterIdentity,
        logicalMeterEntity
      );

      mockMeasurement(
        random,
        physicalMeterEntity,
        measurementCount,
        firstDeliveryInstant,
        reportIntervalInMinutes
      );
    }

    settingUseCases.setDemoDataLoaded();
  }

  private GatewayEntity mockGateway(String serial) {
    return gatewayRepository.save(new GatewayEntity(serial, "2100"));
  }

  private void mockMeasurement(
    Random random,
    PhysicalMeterEntity physicalMeterEntity,
    int measurementCount,
    Instant firstDeliveryInstant,
    int reportIntervalInMinutes
  ) {

    List<MeasurementEntity> measurementEntities = new ArrayList<>();
    for (int j = 0; j < measurementCount; ++j) {
      MeasurementEntity measurementEntity = new MeasurementEntity(
        Date.from(
          firstDeliveryInstant.plus((long) reportIntervalInMinutes * j, ChronoUnit.MINUTES)
        ),
        "Power",
        random.nextDouble(),
        "mW",
        physicalMeterEntity
      );
      measurementEntities.add(measurementEntity);
    }
    measurementJpaRepository.save(measurementEntities);
  }

  private PhysicalMeterEntity mockPhysicalMeter(
    OrganisationEntity organisationEntity,
    String meterIdentity,
    LogicalMeterEntity logicalMeterEntity
  ) {
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      organisationEntity,
      meterIdentity,
      "Electricity",
      "ELV"
    );
    physicalMeterEntity.logicalMeterId = logicalMeterEntity.id;
    return physicalMeterRepository.save(physicalMeterEntity);
  }

  private LogicalMeterEntity mockLogicalMeterEntity(
    String meterIdentity,
    List<GatewayEntity> gatewayEntities,
    int seed
  ) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity();
    logicalMeterEntity.propertyCollection
      .put("user", new UserPropertyDto(meterIdentity, "Demo project"))
      .putArray("numbers", asList(1, 2, 3, 17));

    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    logicalMeterEntity.status = "Ok";
    logicalMeterEntity.meterDefinition = null;
    logicalMeterEntity.created = created;
    logicalMeterEntity.gateways = gatewayEntities;
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.confidence = 0.8;
    locationEntity.latitude = 57.5052592;
    locationEntity.longitude = 12.0683196;
    locationEntity.streetAddress = "Kabelgatan 2T";
    locationEntity.city = "Kungsbacka";
    locationEntity.country = "Sweden";
    logicalMeterEntity.setLocation(locationEntity);

    return logicalMeterJpaRepository.save(logicalMeterEntity);
  }
}
