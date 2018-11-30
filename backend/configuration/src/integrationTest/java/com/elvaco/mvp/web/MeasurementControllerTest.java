package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper.toEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerTest extends IntegrationTest {

  private static final Offset<Double> OFFSET = within(0.000_000_000_000_1);

  private static final int ENERGY_VALUE = 9999;
  private static final double DIFF_TEMP_VALUE_CELSIUS = 285.59;
  private static final double DIFF_TEMP_VALUE_KELVIN = 558.74;

  @Autowired
  private MeterDefinitions meterDefinitions;

  private OrganisationEntity otherOrganisation;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());

    otherOrganisation = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(randomUUID())
        .name("Wayne Industries")
        .slug("wayne-industries")
        .externalId("wayne-industries")
        .build()
    );
  }

  @After
  public void tearDown() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
    }
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(
      heatMeter,
      date
    );

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?resolution=hour"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId()
          + "&after=" + date
          + "&before=" + date.plusHours(1),
        MeasurementDto.class
      )
      .getBody();

    List<String> quantities = measurements.stream()
      .map(m -> m.quantity)
      .collect(toList());

    assertThat(quantities).contains("Difference temperature");
  }

  @Test
  public void measurementUnitScaled() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(heatMeter, date);

    List<MeasurementSeriesDto> measurements = asUser()
      .getList(
        "/measurements?quantity=Difference+temperature:K"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1),
        MeasurementSeriesDto.class
      )
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Difference temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).values.get(0).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void canNotSeeMeasurementsFromMeterBelongingToOtherOrganisation() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity otherOrganisationsMeter = newHeatMeterBelongingTo(
      otherOrganisation,
      date
    );

    newDiffTempMeasurementCelcius(otherOrganisationsMeter, date);

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?logicalMeterId=" + otherOrganisationsMeter.getLogicalMeterId()
          + "&after=" + date
          + "&before=" + date,
        MeasurementDto.class
      )
      .getBody();

    assertThat(measurements).isEmpty();
  }

  @Test
  public void superAdminCanSeeAllMeasurements() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");

    PhysicalMeterEntity firstOrganisationsMeter = newHeatMeterBelongingTo(
      otherOrganisation,
      date
    );
    newDiffTempMeasurementCelcius(
      firstOrganisationsMeter,
      date
    );

    PhysicalMeterEntity secondOrganisationsMeter = newHeatMeterBelongingTo(
      context().organisationEntity,
      date
    );
    newDiffTempMeasurementCelcius(
      secondOrganisationsMeter,
      date
    );

    assertThat(getListAsSuperAdmin(
      "/measurements?logicalMeterId=" + firstOrganisationsMeter.getLogicalMeterId().toString()
        + "," + secondOrganisationsMeter.getLogicalMeterId().toString()
        + "&quantity=Difference+temperature"
        + "&resolution=hour"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
      )
    ).hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriod() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter();
    newDiffTempMeasurementCelcius(heatMeter, date.minusHours(1));
    newDiffTempMeasurementCelcius(heatMeter, date.plusHours(1));
    newEnergyMeasurement(heatMeter, date.minusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "quantity=Difference+temperature"
        + "&logicalMeterId=" + heatMeter.getLogicalMeterId()
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterInPeriod() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(heatMeter, date);
    newDiffTempMeasurementCelcius(heatMeter, date.plusHours(2));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "logicalMeterId=" + heatMeter.getLogicalMeterId()
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents)
      .hasSize(MeterDefinition.DISTRICT_HEATING_METER.quantities.size())
      .contains(
        new MeasurementSeriesDto(
          heatMeter.getLogicalMeterId().toString(),
          "Difference temperature",
          "K",
          heatMeter.externalId,
          MeterDefinition.DISTRICT_HEATING_METER.medium,
          asList(
            new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          )
        ),
        new MeasurementSeriesDto(
          heatMeter.getLogicalMeterId().toString(),
          "Energy",
          "kWh",
          heatMeter.externalId,
          MeterDefinition.DISTRICT_HEATING_METER.medium,
          asList(
            new MeasurementValueDto(date.toInstant(), null),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          )
        )
      );
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriodWithNonDefaultUnit() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(heatMeter, date.minusHours(1));
    newDiffTempMeasurementCelcius(heatMeter, date.plusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantity=Difference+temperature:K"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&logicalMeterId=" + heatMeter.getLogicalMeterId().toString()
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");

    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(heatMeter, date);
    newDiffTempMeasurementCelcius(heatMeter, date.plusHours(1));
    newEnergyMeasurement(heatMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature:K,Energy:kWh"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId().toString()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        heatMeter.getLogicalMeterId().toString(),
        "Difference temperature",
        "K",
        heatMeter.externalId,
        MeterDefinition.DISTRICT_HEATING_METER.medium,
        asList(
          new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
          new MeasurementValueDto(date.plusHours(1).toInstant(), DIFF_TEMP_VALUE_KELVIN)
        )
      ),
      new MeasurementSeriesDto(
        heatMeter.getLogicalMeterId().toString(),
        "Energy",
        "kWh",
        heatMeter.externalId,
        MeterDefinition.DISTRICT_HEATING_METER.medium,
        asList(
          new MeasurementValueDto(date.toInstant(), null),
          new MeasurementValueDto(date.plusHours(1).toInstant(), null)
        )
      )
    );
  }

  @Test
  public void measurementSeriesAreLabeledWithMeterExternalId() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter(date);
    newDiffTempMeasurementCelcius(heatMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId().toString()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.label).isEqualTo(heatMeter.externalId);
  }

  @Test
  public void unknownUnitSuppliedForScaling() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter();
    newDiffTempMeasurementCelcius(heatMeter, date);

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:unknownUnit"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId().toString()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1),
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message)
      .isEqualTo("Can not convert to unknown unit 'unknownUnit'");
  }

  @Test
  public void wrongDimensionForQuantitySuppliedForScaling() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity heatMeter = newHeatMeter();
    newDiffTempMeasurementCelcius(heatMeter, date);

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:kWh"
          + "&logicalMeterId=" + heatMeter.getLogicalMeterId().toString()
          + "&after=" + date
          + "&before=" + date.plusHours(1)
          + "&resolution=hour",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).matches("Can not convert from unit '.*' to 'kWh'");
  }

  @Test
  public void missingMetersParametersReturnsHttp400() {
    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:kWh",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'logicalMeterId' parameter.");
  }

  @Test
  public void consumptionSeriesIsDisplayedWithConsumptionValuesAtFirstTimeInInterval() {
    ZonedDateTime when = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    LogicalMeterEntity consumptionMeter = newLogicalMeterEntity(

      new MeterDefinition(
        MeterDefinitionType.UNKNOWN_METER_TYPE,
        "Consumption of things",
        singleton(Quantity.VOLUME),
        false
      )
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.getLogicalMeterId());
    newMeasurement(meter, when, "Volume", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume", 55.0, "m³");

    List<MeasurementSeriesDto> list = asUser()
      .getList(
        "/measurements?resolution=hour&quantity=Volume"
          + "&logicalMeterId=" + consumptionMeter.getId().id
          + "&after=" + when
          + "&before=" + when.plusHours(2),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(list).containsExactly(
      new MeasurementSeriesDto(
        meter.getLogicalMeterId().toString(),
        "Volume",
        "m³",
        meter.externalId,
        consumptionMeter.meterDefinition.medium,
        asList(
          new MeasurementValueDto(when.toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 20.0),
          new MeasurementValueDto(when.plusHours(2).toInstant(), null)
        )
      )
    );
  }

  @Test
  public void consumptionIsIncludedForValueDirectAfterPeriod() {
    ZonedDateTime when = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    LogicalMeterEntity consumptionMeter = newLogicalMeterEntity(
      new MeterDefinition(
        MeterDefinitionType.UNKNOWN_METER_TYPE,
        "Consumption of things",
        singleton(Quantity.VOLUME),
        false
      )
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.getLogicalMeterId());
    newMeasurement(meter, when, "Volume", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume", 55.0, "m³");

    List<MeasurementSeriesDto> seriesDto = asUser()
      .getList(String.format(
        "/measurements?resolution=hour&quantity=Volume&logicalMeterId=%s"
          + "&after=%s&before=%s",
        consumptionMeter.getId().id,
        when,
        when.plusHours(1)
      ), MeasurementSeriesDto.class).getBody();

    assertThat(seriesDto).containsExactly(
      new MeasurementSeriesDto(
        meter.getLogicalMeterId().toString(),
        "Volume",
        "m³",
        meter.externalId,
        consumptionMeter.meterDefinition.medium,
        asList(
          new MeasurementValueDto(when.plusHours(0).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 20.0)
        )
      )
    );
  }

  @Test
  public void findsConsumptionForGasMeters() {
    ZonedDateTime when = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, when, "Volume", 1.0, "m^3");
    newMeasurement(meter, when.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, when.plusHours(2), "Volume", 5.0, "m^3");

    List<MeasurementSeriesDto> response = asUser()
      .getList(String.format(
        "/measurements"
          + "?after=" + when
          + "&before=" + when.plusHours(2)
          + "&quantity=" + Quantity.VOLUME.name
          + "&logicalMeterId=%s",
        logicalMeter.getId().id
      ), MeasurementSeriesDto.class).getBody();

    ZonedDateTime periodStartHour = when.truncatedTo(ChronoUnit.HOURS);

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(
          periodStartHour.toInstant(),
          1.0
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(1).toInstant(),
          3.0
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(2).toInstant(),
          null
        )
      );
  }

  @Test
  public void measurementsForNonPresentQuantity() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:59:10Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(GAS_METER);
    newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());

    ResponseEntity<ErrorMessageDto> responseEntity = asUser()
      .get(
        "/measurements"
          + "?after=" + after
          + "&before=" + before
          + "&quantity=Floop"
          + "&logicalMeterId=" + logicalMeter.getId().id,
        ErrorMessageDto.class
      );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Floop' for Gas meter");
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    if (isPostgresDialect()) {
      organisationJpaRepository.delete(otherOrganisation);
    }
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return toEntity(meterDefinitions.save(meterDefinition));
  }

  private void newEnergyMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    newMeasurement(meter, created, "Energy", ENERGY_VALUE, "J");
  }

  private void newDiffTempMeasurementCelcius(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    newMeasurement(meter, created, "Difference temperature", DIFF_TEMP_VALUE_CELSIUS, "°C");
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit
  ) {
    measurements.save(Measurement.builder()
      .created(created)
      .quantity(QuantityAccess.singleton().getByName(quantity).name)
      .physicalMeter(PhysicalMeterEntityMapper.toDomainModel(meter))
      .value(value)
      .unit(unit)
      .build()
    );
  }

  private LogicalMeterEntity newLogicalMeterEntity(MeterDefinition meterDefinition) {
    UUID uuid = randomUUID();
    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      uuid,
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity,
      DEFAULT_UTC_OFFSET
    ));
  }

  private PhysicalMeterEntity newHeatMeterBelongingTo(
    OrganisationEntity organisationEntity,
    ZonedDateTime created
  ) {
    return newPhysicalMeterEntity(organisationEntity, created);
  }

  private PhysicalMeterEntity newHeatMeter() {
    return newPhysicalMeterEntity(context().organisationEntity, ZonedDateTime.now());
  }

  private PhysicalMeterEntity newHeatMeter(ZonedDateTime created) {
    return newPhysicalMeterEntity(context().organisationEntity, created);
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
      toEntity(MeterDefinition.DISTRICT_HEATING_METER),
      DEFAULT_UTC_OFFSET
    ));

    UUID physicalMeterId = randomUUID();
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
    UUID uuid = randomUUID();
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

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }
}
