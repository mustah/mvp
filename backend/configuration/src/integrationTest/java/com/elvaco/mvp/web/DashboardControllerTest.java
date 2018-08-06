package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class DashboardControllerTest extends IntegrationTest {

  private final ZonedDateTime startDate = ZonedDateTime.parse("2018-08-01T00:00:00.00Z");
  private final ZonedDateTime beforeDate = ZonedDateTime.parse("2018-08-03T00:00:00.00Z");

  private double readingCount = 0.0;
  private double readingFailedCount = 0.0;

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
    assumeTrue(isPostgresDialect());
  }

  @After
  public void tearDown() {
  }

  @Test
  public void collectionStatusNoPeriod_ReturnsEmptyCollectionStatus() {
    ResponseEntity<DashboardDto> response = asTestUser()
      .get("/dashboards/current", DashboardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    WidgetDto widget = response.getBody().widgets.get(0);
    assertThat(widget.total).isEqualTo(0);
    assertThat(widget.pending).isEqualTo(0);
    assertThat(widget.type).isEqualTo(WidgetType.COLLECTION.name);
  }

  @Test
  public void findAllWithCollectionStatusForMediumGas() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.GAS_METER),
      startDate
    );

    List<PhysicalMeterEntity> physicalMeters = Arrays.asList(
      newPhysicalMeterEntity(logicalMeter.id, MeterDefinition.GAS_METER),
      newPhysicalMeterEntity(logicalMeter.id, MeterDefinition.GAS_METER)
    );

    newActiveStatusLogs(physicalMeters, startDate);

    createMeasurementMockData(
      physicalMeters,
      startDate,
      Duration.between(startDate, beforeDate).toDays()
    );

    ResponseEntity<DashboardDto> response = asTestUser()
      .get(
        "/dashboards/current?medium=Gas&after=" + startDate + "&before=" + beforeDate,
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().widgets.get(0))
      .isEqualTo(new WidgetDto(WidgetType.COLLECTION.name, 12, 6));
  }

  @Test
  public void findAllWithCollectionStatusRoomSensor() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER),
      startDate
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeterEntity(logicalMeter.id, MeterDefinition.DISTRICT_HEATING_METER)
    );

    newActiveStatusLogs(physicalMeters, startDate);

    createMeasurementMockData(
      physicalMeters,
      startDate,
      Duration.between(startDate, beforeDate).toDays()
    );

    ResponseEntity<DashboardDto> response = asTestUser()
      .get(
        "/dashboards/current?after=" + startDate + "&before=" + beforeDate,
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DashboardDto dashboardDtos = response.getBody();

    assertThat(dashboardDtos.widgets.get(0).type)
      .as("Unexpected widget type")
      .isEqualTo(WidgetType.COLLECTION.name);

    assertThat(dashboardDtos.widgets.get(0).total)
      .as("Expected number of measurements diverged")
      .isEqualTo(readingCount + readingFailedCount);

    assertThat(dashboardDtos.widgets.get(0).pending)
      .as("Unexpected number of missing measurements")
      .isEqualTo(readingFailedCount);
  }

  private void newActiveStatusLogs(
    List<PhysicalMeterEntity> physicalMeters,
    ZonedDateTime startDate
  ) {
    List<PhysicalMeterStatusLogEntity> statuses = physicalMeters
      .stream()
      .map(physicalMeterEntity ->
        new PhysicalMeterStatusLogEntity(
          null,
          physicalMeterEntity.id,
          StatusType.ACTIVE,
          startDate,
          null
        )).collect(
        toList());

    physicalMeterStatusLogJpaRepository.save(statuses);
  }

  private void createMeasurementMockData(
    List<PhysicalMeterEntity> physicalMeters,
    ZonedDateTime startDate,
    long dayCount
  ) {
    for (PhysicalMeterEntity meter : physicalMeters) {
      measurementJpaRepository.save(createMeasurements(
        meter,
        startDate,
        meter.readIntervalMinutes,
        dayCount * TimeUnit.DAYS.toMinutes(1) / meter.readIntervalMinutes
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

    for (int i = 0; i < values; i++) {
      if (i % 2 == 0) {
        readingFailedCount++;
        continue;
      }

      ZonedDateTime created = measurementDate.plusMinutes(i * interval);

      switch (physicalMeterEntity.medium) {
        case "District heating":
          measurementEntities.addAll(DemoDataHelper.heatMeasurement(created, physicalMeterEntity));
          break;
        case "Gas":
          measurementEntities.addAll(DemoDataHelper.gasMeasurement(
            created,
            physicalMeterEntity,
            40
          ));
          break;
        default:
          throw new RuntimeException(
            "Medium '" + physicalMeterEntity.medium + "' is not implemented in createMeasurements"
          );
      }

      readingCount++;
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

  private PhysicalMeterEntity newPhysicalMeterEntity(
    UUID logicalMeterId,
    MeterDefinition meterDefinition
  ) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      meterDefinition.medium,
      "",
      logicalMeterId,
      TimeUnit.DAYS.toMinutes(1),
      emptySet()
    ));
  }
}
