package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MissingMeasurementControllerTest extends IntegrationTest {

  private ZonedDateTime startDate;
  private ZonedDateTime endDate;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());

    endDate = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);
    startDate = endDate.minusMinutes(10);
  }

  @After
  public void tearDown() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
      missingMeasurementJpaRepository.refreshLocked();
    }
  }

  @Test
  public void findMissingMeterReadings_WithoutPeriod() {
    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(new RequestParametersAdapter());

    assertThat(missingMeterReadingsCounts).isEmpty();
  }

  @Test
  public void findMissingMeterReadings_WhenNoneExists() {
    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts).isEmpty();
  }

  @Test
  public void refreshAsSuperAdmin() {
    LogicalMeterEntity logicalMeter = newLogicalMeter(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.GAS_METER),
      startDate.minusMinutes(11)
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeter(logicalMeter.getLogicalMeterId())
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(11));

    ResponseEntity<Void> response = asSuperAdmin()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    RequestParameters parameters = makeParametersWithDateRange();

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(parameters);

    assertThat(missingMeterReadingsCounts.size()).isEqualTo(1);
    assertThat(missingMeterReadingsCounts.get(0).missingReadingCount).isEqualTo(10);
  }

  @Test
  public void refreshAsUserDenied() {
    LogicalMeterEntity logicalMeter = newLogicalMeter(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.GAS_METER),
      startDate.minusMinutes(11)
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeter(logicalMeter.getLogicalMeterId())
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(11));

    ResponseEntity<Void> response = asUser()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts = logicalMeterJpaRepository
      .findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts.size()).isEqualTo(1);
    assertThat(missingMeterReadingsCounts.get(0).missingReadingCount).isEqualTo(0);
  }

  @Test
  public void refreshAsAdminDenied() {
    LogicalMeterEntity logicalMeter = newLogicalMeter(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.GAS_METER),
      startDate.minusMinutes(11)
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeter(logicalMeter.getLogicalMeterId())
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(11));

    ResponseEntity<Void> response = asAdmin()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts = logicalMeterJpaRepository
      .findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts.size()).isEqualTo(1);
    assertThat(missingMeterReadingsCounts.get(0).missingReadingCount).isEqualTo(0);
  }

  private RequestParameters makeParametersWithDateRange() {
    return new RequestParametersAdapter()
      .add(AFTER, startDate.toString())
      .add(BEFORE, endDate.toString());
  }

  private void newActiveStatusLogs(
    List<PhysicalMeterEntity> physicalMeters,
    ZonedDateTime startDate
  ) {
    List<PhysicalMeterStatusLogEntity> statuses = physicalMeters.stream()
      .map(physicalMeterEntity ->
        new PhysicalMeterStatusLogEntity(
          null,
          physicalMeterEntity.id,
          StatusType.OK,
          startDate,
          null
        )).collect(toList());

    physicalMeterStatusLogJpaRepository.saveAll(statuses);
  }

  private LogicalMeterEntity newLogicalMeter(
    MeterDefinitionEntity meterDefinition,
    ZonedDateTime created
  ) {
    UUID uuid = randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      randomUUID(),
      uuid.toString(),
      context().organisationId(),
      created,
      meterDefinition,
      DEFAULT_UTC_OFFSET
    ));
  }

  private PhysicalMeterEntity newPhysicalMeter(UUID logicalMeterId) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationId(),
      "",
      uuid.toString(),
      MeterDefinition.GAS_METER.medium,
      "",
      logicalMeterId,
      (long) 1,
      1,
      1,
      emptySet(),
      emptySet()
    ));
  }
}
