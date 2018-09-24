package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerPagedTest extends IntegrationTest {

  private static final MeterDefinition BUTTER_METER_DEFINITION = new MeterDefinition(
    MeterDefinitionType.UNKNOWN_METER_TYPE,
    "Butter",
    new HashSet<>(asList(Quantity.DIFFERENCE_TEMPERATURE, Quantity.ENERGY)),
    false
  );

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private MeterDefinitions meterDefinitions;

  private OrganisationEntity otherOrganisation;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());

    otherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Wayne Industries",
        "wayne-industries",
        "wayne-industries"
      )
    );
  }

  @After
  public void tearDown() {
    if (!isPostgresDialect()) {
      return;
    }

    measurementJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    organisationJpaRepository.delete(otherOrganisation);
  }


  @Test
  public void pagedMeasurementsFiltered() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T06:00:00Z[UTC]");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(GAS_METER);

    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");
    newMeasurement(meter, after.plusHours(3), "Volume", 6.0, "m^3");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(GAS_METER);

    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);

    newMeasurement(meter2, after.plusHours(4), "Volume", 7.0, "m^3");

    org.springframework.data.domain.Page<MeasurementDto> response = asTestUser()
      .getPage(String.format(
        "/measurements/paged/?after=%s&before=%s&logicalMeterId=%s&size=2&sort=created,desc",
        after, before, logicalMeter.getId()
      ), MeasurementDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(2);

    List<MeasurementDto> content = response.getContent();
    assertThat(content.size()).isEqualTo(2);

    assertThat(content.get(0)).isEqualTo(new MeasurementDto(
      "Volume",
      6.0,
      "m³",
      after.plusHours(3)
    ));

    assertThat(content.get(1)).isEqualTo(new MeasurementDto(
      "Volume",
      5.0,
      "m³",
      after.plusHours(2)
    ));
  }

  @Test
  public void pagedMeasurementsUnableToAccessOtherOrganisationsMeasurements() {
    ZonedDateTime created = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    PhysicalMeterEntity physicalMeter = newButterMeterBelongingTo(
      otherOrganisation,
      created
    );

    newButterTemperatureMeasurement(physicalMeter, created);

    org.springframework.data.domain.Page<MeasurementDto> response = asTestUser()
      .getPage(String.format(
        "/measurements/paged/?logicalMeterId=%s&size=2&sort=created,desc",
        physicalMeter.logicalMeterId
      ), MeasurementDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return MeterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
  }

  private MeasurementEntity newButterTemperatureMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    return newMeasurement(meter, created, "Difference temperature", 285.59, "°C");
  }

  private MeasurementEntity newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit
  ) {
    return measurementJpaRepository.save(new MeasurementEntity(
      created,
      QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(quantity)),
      new MeasurementUnit(unit, value),
      meter
    ));
  }

  private LogicalMeterEntity newLogicalMeterEntity(MeterDefinition meterDefinition) {
    UUID uuid = randomUUID();
    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      uuid,
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
    ));
  }

  private PhysicalMeterEntity newButterMeterBelongingTo(
    OrganisationEntity organisationEntity,
    ZonedDateTime created
  ) {
    return newPhysicalMeterEntity(organisationEntity, created);
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(
    OrganisationEntity organisationEntity,
    ZonedDateTime created
  ) {
    UUID logicalMeterId = randomUUID();
    logicalMeterJpaRepository.save(new LogicalMeterEntity(
      logicalMeterId,
      logicalMeterId.toString(),
      organisationEntity.id,
      created,
      saveMeterDefinition(MeasurementControllerPagedTest.BUTTER_METER_DEFINITION)
    ));

    UUID physicalMeterId = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      physicalMeterId,
      organisationEntity,
      "",
      physicalMeterId.toString(),
      "",
      "",
      logicalMeterId,
      0,
      emptySet(),
      emptySet()
    ));
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(UUID logicalMeterId) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      logicalMeterId,
      0,
      emptySet(),
      emptySet()
    ));
  }

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }
}
