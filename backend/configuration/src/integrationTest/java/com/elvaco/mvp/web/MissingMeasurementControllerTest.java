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
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.MissingMeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private MissingMeasurementJpaRepository missingMeasurementJpaRepository;

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
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    if (isPostgresDialect()) {
      missingMeasurementJpaRepository.refreshLocked();
    }
  }

  @Test
  public void refreshAsSuperAdmin() {
    LogicalMeterEntity logicalMeter = newLogicalMeter(
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.GAS_METER),
      startDate.minusMinutes(11)
    );

    List<PhysicalMeterEntity> physicalMeters = singletonList(
      newPhysicalMeter(logicalMeter.id)
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

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts = logicalMeterJpaRepository
      .findMissingMeterReadingsCounts(parameters);

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
      newPhysicalMeter(logicalMeter.id)
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(11));

    ResponseEntity<Void> response = asTestUser()
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
      newPhysicalMeter(logicalMeter.id)
    );

    newActiveStatusLogs(physicalMeters, startDate.minusMinutes(11));

    ResponseEntity<Void> response = asTestAdmin()
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

    physicalMeterStatusLogJpaRepository.save(statuses);
  }

  private LogicalMeterEntity newLogicalMeter(
    MeterDefinitionEntity meterDefinition,
    ZonedDateTime created
  ) {
    UUID uuid = randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      randomUUID(),
      uuid.toString(),
      context().organisationEntity.id,
      created,
      meterDefinition
    ));
  }

  private PhysicalMeterEntity newPhysicalMeter(UUID logicalMeterId) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      MeterDefinition.GAS_METER.medium,
      "",
      logicalMeterId,
      (long) 1,
      emptySet(),
      emptySet()
    ));
  }
}
