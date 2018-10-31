package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.HashSet;
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
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeasurementDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
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
  private MeterDefinitions meterDefinitions;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
  }

  @After
  public void tearDown() {
    if (!isPostgresDialect()) {
      return;
    }

    measurementJpaRepository.deleteAll();
  }

  @Test
  public void isPageable() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T06:00:00Z[UTC]");
    LogicalMeterEntity logicalGasMeter = newLogicalMeterEntity(GAS_METER);

    PhysicalMeterEntity gasMeter = newPhysicalMeterEntity(logicalGasMeter.id);
    newMeasurement(gasMeter, after, "Volume", 1.0, "m^3");
    newMeasurement(gasMeter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(gasMeter, after.plusHours(2), "Volume", 5.0, "m^3");
    newMeasurement(gasMeter, after.plusHours(3), "Volume", 6.0, "m^3");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(GAS_METER);

    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);

    newMeasurement(meter2, after.plusHours(4), "Volume", 7.0, "m^3");

    org.springframework.data.domain.Page<MeasurementDto> firstPage = asUser()
      .getPage(String.format(
        "/measurements/paged?after=%s&before=%s&logicalMeterId=%s&size=2",
        after, before, logicalGasMeter.getId()
      ), MeasurementDto.class);

    assertThat(firstPage.getTotalElements()).isEqualTo(4);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);

    assertThat(firstPage.getContent())
      .hasSize(2)
      .containsExactlyInAnyOrder(
        new MeasurementDto(
          "Volume",
          6.0,
          "m³",
          after.plusHours(3)
        ),
        new MeasurementDto(
          "Volume",
          5.0,
          "m³",
          after.plusHours(2)
        )
      );
  }

  @Test
  public void unableToAccessOtherOrganisation() {
    ZonedDateTime created = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    PhysicalMeterEntity physicalMeter = newPhysicalMeterEntity(
      context().organisationEntity2,
      created
    );

    newMeasurement(physicalMeter, created, "Difference temperature", 285.59, "°C");

    Page<MeasurementDto> wrongUserResponse = asUser()
      .getPage(String.format(
        "/measurements/paged?logicalMeterId=%s",
        physicalMeter.logicalMeterId
      ), MeasurementDto.class);

    assertThat(wrongUserResponse).hasSize(0);

    Page<MeasurementDto> correctUserResponse = asOtherUser()
      .getPage(String.format(
        "/measurements/paged?logicalMeterId=%s",
        physicalMeter.logicalMeterId
      ), MeasurementDto.class);

    assertThat(correctUserResponse).hasSize(1);
  }

  @Test
  public void defaultsToDecidedUponUnits() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    ZonedDateTime before = after.plusHours(1);
    LogicalMeterEntity districtHeatingMeter = newLogicalMeterEntity(DISTRICT_HEATING_METER);

    PhysicalMeterEntity meter = newPhysicalMeterEntity(districtHeatingMeter.id);
    newMeasurement(meter, after, "Energy", 1.0, "GJ");

    org.springframework.data.domain.Page<MeasurementDto> firstPage = asUser()
      .getPage(String.format(
        "/measurements/paged/?after=%s&before=%s&logicalMeterId=%s",
        after, before, districtHeatingMeter.getId()
      ), MeasurementDto.class);

    assertThat(firstPage.getContent())
      .hasSize(1)
      .extracting("unit")
      .containsExactly("kWh");
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return MeterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
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
      1,
      1,
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
      1,
      1,
      emptySet(),
      emptySet()
    ));
  }
}
