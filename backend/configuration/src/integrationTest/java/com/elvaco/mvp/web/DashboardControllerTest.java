package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.ACTIVE;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DashboardControllerTest extends IntegrationTest {

  private final Random random = new Random();
  private final MeterDefinitionMapper meterDefinitionMapper = new MeterDefinitionMapper();
  private double measurementCount = 0.0;
  private double measurementFailedCount = 0.0;
  private final ZonedDateTime startDate = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");
  private final ZonedDateTime beforeDate = ZonedDateTime.parse("2001-01-11T00:00:00.00Z");

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Before
  public void setUp() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER),
      startDate
    );

    List<PhysicalMeterEntity> physicalMeters = asList(
      newPhysicalMeterEntity(logicalMeter.id, 1440),
      newPhysicalMeterEntity(logicalMeter.id, 1440),
      newPhysicalMeterEntity(logicalMeter.id, 1440)
    );

    newStatusLogs(
      physicalMeters,
      startDate,
      ACTIVE
    );

    createMeasurementMockData(
      physicalMeters,
      startDate,
      Duration.between(startDate, beforeDate).toDays()
    );
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void findAllWithCollectionStatusNoPeriods() {
    ResponseEntity<DashboardDto> response = asTestUser()
      .get(
        "/dashboards/current"
        + "?status=active",
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DashboardDto dashboardDtos = response.getBody();

    assertThat(dashboardDtos.widgets.size())
      .as("Unexpected number of widgets")
      .isEqualTo(0);
  }

  @Test
  public void findAllWithCollectionStatus() {
    ResponseEntity<DashboardDto> response = asTestUser()
      .get(
        "/dashboards/current"
        + "?after=" + startDate
        + "&before=" + beforeDate
        + "&status=active",
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DashboardDto dashboardDtos = response.getBody();

    assertThat(dashboardDtos.widgets.get(0).type)
      .as("Unexpected widget type")
      .isEqualTo(WidgetType.COLLECTION.name);

    assertThat(dashboardDtos.widgets.get(0).total)
      .as("Expected number of measurements diverged")
      .isEqualTo(measurementCount + measurementFailedCount);

    assertThat(dashboardDtos.widgets.get(0).pending)
      .as("Unexpected number of remaining measurements")
      .isEqualTo(measurementFailedCount);
  }

  private List<PhysicalMeterStatusLogEntity> newStatusLogs(
    List<PhysicalMeterEntity> physicalMeters,
    ZonedDateTime startDate,
    StatusType statusEntity
  ) {
    List<PhysicalMeterStatusLogEntity> statuses = physicalMeters
      .stream()
      .map(physicalMeterEntity ->
        new PhysicalMeterStatusLogEntity(
          null,
          physicalMeterEntity.id,
          startDate,
          null,
          statusEntity
        )).collect(
        toList());

    return physicalMeterStatusLogJpaRepository.save(statuses);
  }

  private void createMeasurementMockData(
    List<PhysicalMeterEntity> meters,
    ZonedDateTime startDate,
    long dayCount
  ) {

    for (PhysicalMeterEntity meter : meters) {
      measurementJpaRepository.save(createMeasurements(
        meter,
        startDate,
        meter.readIntervalMinutes,
        dayCount * 1440 / meter.readIntervalMinutes
      ));
    }
  }

  private List<MeasurementEntity> createMeasurements(
    PhysicalMeterEntity physicalMeterEntity,
    ZonedDateTime measurementDate,
    long interval,
    long values
  ) {
    List<MeasurementEntity> measurementEntities = new ArrayList<>();

    for (int x = 0; x < values; x++) {
      if (random.nextInt(10) >= 8) {
        measurementFailedCount = measurementFailedCount + 7;
        continue;
      }

      ZonedDateTime created = measurementDate.plusMinutes(x * interval);

      measurementEntities.addAll(
        DemoDataHelper.getDistrictHeatingMeterReading(created, physicalMeterEntity)
      );

      measurementCount = measurementCount + 7;
    }

    return measurementEntities;
  }

  private LogicalMeterEntity newLogicalMeterEntity(
    MeterDefinitionEntity meterDefinitionEntity,
    ZonedDateTime created
  ) {
    UUID uuid = randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      randomUUID(),
      uuid.toString(),
      context().organisationEntity.id,
      created,
      meterDefinitionEntity
    ));
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(UUID logicalMeterId, int readInterval) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      logicalMeterId,
      readInterval
    ));
  }
}
