package com.elvaco.mvp.bootstrap.demo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.gateway.GatewayEntity;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.repository.jpa.GatewayRepository;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.PhysicalMeterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.fixture.Entities.ELVACO_ENTITY;
import static com.elvaco.mvp.fixture.Entities.WAYNE_INDUSTRIES_ENTITY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Profile("demo")
@Component
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

  private final MeteringPointJpaRepository meteringPointJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final PhysicalMeterRepository physicalMeterRepository;
  private final OrganisationRepository organisationRepository;
  private final GatewayRepository gatewayRepository;
  private final SettingUseCases settingUseCases;

  @Autowired
  public DemoDataLoader(
    MeteringPointJpaRepository meteringPointJpaRepository,
    MeasurementJpaRepository measurementJpaRepository,
    PhysicalMeterRepository physicalMeterRepository,
    OrganisationRepository organisationRepository,
    GatewayRepository gatewayRepository,
    SettingUseCases settingUseCases
  ) {
    this.meteringPointJpaRepository = meteringPointJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.physicalMeterRepository = physicalMeterRepository;
    this.organisationRepository = organisationRepository;
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

    organisationRepository.save(asList(ELVACO_ENTITY, WAYNE_INDUSTRIES_ENTITY));

    int meters = 10;
    int daysPerMeter = 30;
    int reportIntervalInMinutes = 15;
    int measurementCount = daysPerMeter * 24 * 4; // One day, 15-minute delivery interval
    Instant firstDeliveryInstant = Instant.now().minus(daysPerMeter, ChronoUnit.DAYS);

    for (int i = 0; i < meters; ++i) {
      String meterIdentity = "DEMO-METER-" + i;
      String serial = "DEMO-GATEWAY-" + i;

      List<GatewayEntity> gatewayEntities = singletonList(mockGateway(serial));

      MeteringPointEntity meteringPointEntity = mockMeteringPoint(
        meterIdentity,
        gatewayEntities,
        i);

      PhysicalMeterEntity physicalMeterEntity = mockPhysicalMeter(
        ELVACO_ENTITY,
        meterIdentity,
        meteringPointEntity
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
    GatewayEntity gatewayEntity = new GatewayEntity();
    gatewayEntity.model = "2100";
    gatewayEntity.serial = serial;

    return gatewayRepository.save(gatewayEntity);
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
    MeteringPointEntity meteringPointEntity
  ) {
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      organisationEntity,
      meterIdentity,
      "Electricity"
    );
    physicalMeterEntity.meteringPoint = meteringPointEntity;
    physicalMeterRepository.save(physicalMeterEntity);
    return physicalMeterEntity;
  }

  private MeteringPointEntity mockMeteringPoint(
    String meterIdentity,
    List<GatewayEntity> gatewayEntities,
    int seed
  ) {
    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.propertyCollection
      .put("user", new UserPropertyDto(meterIdentity, "Demo project"))
      .putArray("numbers", asList(1, 2, 3, 17))
      .put("latitude", 1.1)
      .put("longitude", 1.1)
      .put("confidence", 1.1);

    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    meteringPointEntity.status = "Ok";
    meteringPointEntity.medium = "Water";
    meteringPointEntity.created = created;
    meteringPointEntity.gateways = gatewayEntities;

    return meteringPointJpaRepository.save(meteringPointEntity);
  }
}
