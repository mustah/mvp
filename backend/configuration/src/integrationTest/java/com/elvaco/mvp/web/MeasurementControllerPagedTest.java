package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeasurementDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerPagedTest extends IntegrationTest {

  @Autowired
  private MeterDefinitionEntityMapper meterDefinitionEntityMapper;

  @Autowired
  private QuantityProvider quantityProvider;

  @Autowired
  private QuantityEntityMapper quantityEntityMapper;

  @Autowired
  private MeterDefinitions meterDefinitions;

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
  public void isPageable() {
    var date = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    var logicalGasMeter = newLogicalMeterEntity(GAS_METER);

    var physicalGasMeter = newPhysicalMeterEntity(logicalGasMeter.getLogicalMeterId());
    newMeasurement(physicalGasMeter, date, "Volume", 1.0, "m^3");
    newMeasurement(physicalGasMeter, date.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(physicalGasMeter, date.plusHours(2), "Volume", 5.0, "m^3");
    newMeasurement(physicalGasMeter, date.plusHours(3), "Volume", 6.0, "m^3");

    var logicalMeter2 = newLogicalMeterEntity(GAS_METER);

    var meter2 = newPhysicalMeterEntity(logicalMeter2.getLogicalMeterId());

    newMeasurement(meter2, date.plusHours(4), "Volume", 7.0, "m^3");

    var url = urlFrom(logicalGasMeter.getId().id);

    Page<MeasurementDto> firstPage = asUser().getPage(url, MeasurementDto.class);

    assertThat(firstPage.getTotalElements()).isEqualTo(4);
    assertThat(firstPage.getTotalPages()).isEqualTo(1);
    assertThat(firstPage.getContent())
      .containsExactlyInAnyOrder(
        new MeasurementDto("Volume", 1.0, "m³", date),
        new MeasurementDto("Volume", 2.0, "m³", date.plusHours(1)),
        new MeasurementDto("Volume", 5.0, "m³", date.plusHours(2)),
        new MeasurementDto("Volume", 6.0, "m³", date.plusHours(3))
      );
  }

  @Test
  public void unableToAccessOtherOrganisation() {
    var created = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    var physicalMeter = newPhysicalMeterEntity(context().organisationEntity2, created);

    newMeasurement(physicalMeter, created, "Difference temperature", 285.59, "°C");

    var url = urlFrom(physicalMeter.getLogicalMeterId());
    Page<MeasurementDto> wrongUserResponse = asUser().getPage(url, MeasurementDto.class);

    assertThat(wrongUserResponse).hasSize(0);

    Page<MeasurementDto> correctUserResponse = asOtherUser().getPage(url, MeasurementDto.class);

    assertThat(correctUserResponse).hasSize(1);
  }

  @Test
  public void defaultsToDecidedUponUnits() {
    var after = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    var districtHeatingMeter = newLogicalMeterEntity(DISTRICT_HEATING_METER);

    var physicalMeter = newPhysicalMeterEntity(districtHeatingMeter.getLogicalMeterId());
    newMeasurement(physicalMeter, after, "Energy", 1.0, "GJ");

    var url = urlFrom(districtHeatingMeter.getId().id);
    Page<MeasurementDto> firstPage = asUser().getPage(url, MeasurementDto.class);

    assertThat(firstPage.getContent()).extracting("unit").containsExactly("kWh");
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return meterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
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
      quantityEntityMapper.toEntity(quantityProvider.getByName(quantity)),
      value,
      meter
    ));
  }

  private LogicalMeterEntity newLogicalMeterEntity(MeterDefinition meterDefinition) {
    var id = randomUUID();
    var meterDefinitionEntity = saveMeterDefinition(meterDefinition);
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      new EntityPk(id, context().organisationEntity.id),
      id.toString(),
      ZonedDateTime.now(),
      meterDefinitionEntity,
      DEFAULT_UTC_OFFSET
    ));
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(
    OrganisationEntity organisationEntity,
    ZonedDateTime created
  ) {
    var logicalMeterId = randomUUID();
    logicalMeterJpaRepository.save(new LogicalMeterEntity(
      new EntityPk(logicalMeterId, organisationEntity.id),
      logicalMeterId.toString(),
      created,
      saveMeterDefinition(MeterDefinition.DISTRICT_HEATING_METER),
      DEFAULT_UTC_OFFSET
    ));

    var physicalMeterId = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      physicalMeterId,
      organisationEntity.id,
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
    var uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationId(),
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

  private static Url urlFrom(UUID logicalMeterId) {
    return Url.builder()
      .path("/measurements/paged/")
      .parameter(LOGICAL_METER_ID, logicalMeterId)
      .build();
  }
}
