package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testing.fixture.UserTestData;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("ALL")
public class DashboardControllerTest extends IntegrationTest {

  private final ZonedDateTime startDate = ZonedDateTime.now(ZoneId.of("UTC"))
    .minusDays(10)
    .truncatedTo(ChronoUnit.DAYS);
  private final ZonedDateTime beforeDate = ZonedDateTime.now(ZoneId.of("UTC"))
    .minusDays(7)
    .truncatedTo(ChronoUnit.DAYS);

  @Autowired
  private MeterDefinitionEntityMapper meterDefinitionEntityMapper;

  @Autowired
  private DemoDataHelper demoDataHelper;

  private double readingCount = 0.0;
  private double readingFailedCount = 0.0;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
  }

  @After
  public void tearDown() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
    }
  }

  @Test
  public void collectionStatusNoPeriod_ReturnsEmptyCollectionStatus() {
    ResponseEntity<DashboardDto> response = asUser()
      .get("/dashboards/current", DashboardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    WidgetDto widget = response.getBody().widgets.get(0);
    assertThat(widget.total).isEqualTo(0);
    assertThat(widget.pending).isEqualTo(0);
    assertThat(widget.type).isEqualTo(WidgetType.COLLECTION.name);
  }

  // TODO[!must!] refactor this test class to not use *JpaRepository classes anymore!
  // TODO[!must!] we should use the core repository classes to build and created entities.
  @Ignore
  @Test
  public void findCollectionStatsEnsureOrganisationFiltersAreApplied() {
    UserSelection selection = UserSelection.builder()
      .selectionParameters(toJsonNode(CITIES_JSON_STRING))
      .organisationId(MARVEL.id)
      .build();

    Organisation organisation = MARVEL.toBuilder()
      .selection(selection)
      .parent(ELVACO)
      .build();

    User user = UserTestData.subOrgUser().organisation(organisation).build();

    var url = Url.builder()
      .path("/dashboards/current")
      .parameter(CITY, "norge,oslo")
      .parameter(BEFORE, "2018-01-01T00:00:00Z")
      .parameter(AFTER, "2018-12-31T00:00:00Z")
      .build();

    var response = as(user).get(url, DashboardDto.class);

    assertThat(response.getBody().widgets).isEmpty();
  }

  @Test
  public void findAllWithCollectionStatusForMediumGas() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionEntityMapper.toEntity(GAS_METER),
      startDate
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeterEntity(logicalMeter.getLogicalMeterId(), GAS_METER)
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(15));

    createMeasurementMockData(
      physicalMeters,
      startDate,
      Duration.between(startDate, beforeDate).toDays()
    );

    missingMeasurementJpaRepository.refreshLocked();

    ResponseEntity<DashboardDto> response = asUser()
      .get(
        "/dashboards/current?medium=Gas&after=" + startDate + "&before=" + beforeDate,
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().widgets.get(0))
      .isEqualTo(new WidgetDto(
        WidgetType.COLLECTION.name,
        readingFailedCount + readingCount,
        readingFailedCount
      ));
  }

  @Test
  public void findAllWithCollectionStatusRoomSensor() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionEntityMapper.toEntity(DISTRICT_HEATING_METER),
      startDate
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeterEntity(logicalMeter.getLogicalMeterId(), DISTRICT_HEATING_METER)
    );

    newActiveStatusLogs(physicalMeters, startDate);

    createMeasurementMockData(
      physicalMeters,
      startDate,
      Duration.between(startDate, beforeDate).toDays()
    );

    missingMeasurementJpaRepository.refreshLocked();

    ResponseEntity<DashboardDto> response = asUser()
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
    physicalMeterStatusLogJpaRepository.saveAll(physicalMeters.stream()
      .map(physicalMeterEntity -> new PhysicalMeterStatusLogEntity(
        null,
        physicalMeterEntity.id,
        StatusType.OK,
        startDate,
        null
      ))
      .collect(toList()));
  }

  private void createMeasurementMockData(
    List<PhysicalMeterEntity> physicalMeters,
    ZonedDateTime startDate,
    long dayCount
  ) {
    for (PhysicalMeterEntity meter : physicalMeters) {
      measurementJpaRepository.saveAll(createMeasurements(
        meter,
        startDate.minusHours(1),
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
          measurementEntities.addAll(demoDataHelper.heatMeasurement(
            created,
            physicalMeterEntity
          ));
          break;
        case "Gas":
          measurementEntities.addAll(demoDataHelper.gasMeasurement(
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
    UUID id = randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      new EntityPk(id, context().organisationEntity.id),
      id.toString(),
      created,
      meterDefinitionEntity,
      DEFAULT_UTC_OFFSET
    ));
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(
    UUID logicalMeterId,
    MeterDefinition meterDefinition
  ) {
    UUID id = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      id,
      context().organisationId(),
      "",
      id.toString(),
      meterDefinition.medium,
      "",
      logicalMeterId,
      TimeUnit.DAYS.toMinutes(1),
      1,
      1,
      emptySet(),
      emptySet()
    ));
  }
}
