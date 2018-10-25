package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
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
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerTest extends IntegrationTest {

  private static final MeterDefinition BUTTER_METER_DEFINITION = new MeterDefinition(
    MeterDefinitionType.UNKNOWN_METER_TYPE,
    "Butter",
    new HashSet<>(asList(Quantity.DIFFERENCE_TEMPERATURE, Quantity.ENERGY)),
    false
  );
  private static final Offset<Double> OFFSET = within(0.000_000_000_000_1);

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
    if (!isPostgresDialect()) {
      return;
    }

    measurementJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    organisationJpaRepository.delete(otherOrganisation);
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity physicalButterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(
      physicalButterMeter,
      date
    );

    List<MeasurementDto> measurements = asTestUser()
      .getList(
        "/measurements?resolution=hour"
          + "&meters=" + physicalButterMeter.logicalMeterId
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
    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);

    List<MeasurementSeriesDto> measurements = asTestUser()
      .getList(
        "/measurements?quantities=Difference+temperature:K"
          + "&meters=" + butterMeter.logicalMeterId
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1),
        MeasurementSeriesDto.class
      )
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Difference temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).values.get(0).value).isEqualTo(558.74, OFFSET);
  }

  @Test
  public void canNotSeeMeasurementsFromMeterBelongingToOtherOrganisation() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity otherOrganisationsMeter = newButterMeterBelongingTo(
      otherOrganisation,
      date
    );

    newDiffTempMeasurementWithValue285_59Celsius(otherOrganisationsMeter, date);

    List<MeasurementDto> measurements = asTestUser()
      .getList(
        "/measurements?meters=" + otherOrganisationsMeter.logicalMeterId
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

    PhysicalMeterEntity firstOrganisationsMeter = newButterMeterBelongingTo(
      otherOrganisation,
      date
    );
    newDiffTempMeasurementWithValue285_59Celsius(
      firstOrganisationsMeter,
      date
    );

    PhysicalMeterEntity secondOrganisationsMeter = newButterMeterBelongingTo(
      context().organisationEntity,
      date
    );
    newDiffTempMeasurementWithValue285_59Celsius(
      secondOrganisationsMeter,
      date
    );

    assertThat(getListAsSuperAdmin(
      "/measurements?meters=" + firstOrganisationsMeter.logicalMeterId.toString()
        + "," + secondOrganisationsMeter.logicalMeterId.toString()
        + "&quantities=Difference+temperature"
        + "&resolution=hour"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
      )
    ).hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriod() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity butterMeter = newButterMeter();
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.minusHours(1));
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.plusHours(1));
    newEnergyMeasurementWithValue9999J(butterMeter, date.minusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "quantities=Difference+temperature"
        + "&meters=" + butterMeter.logicalMeterId
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(558.74, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterInPeriod() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.plusHours(2));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "meters=" + butterMeter.logicalMeterId
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        butterMeter.logicalMeterId.toString(),
        "Difference temperature",
        "K",
        butterMeter.externalId,
        MeasurementControllerTest.BUTTER_METER_DEFINITION.medium,
        asList(
          new MeasurementValueDto(date.toInstant(), 558.74),
          new MeasurementValueDto(date.plusHours(1).toInstant(), null)
        )
      ),
      new MeasurementSeriesDto(
        butterMeter.logicalMeterId.toString(),
        "Energy",
        "kWh",
        butterMeter.externalId,
        MeasurementControllerTest.BUTTER_METER_DEFINITION.medium,
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
    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.minusHours(1));
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.plusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Difference+temperature:K"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&meters=" + butterMeter.logicalMeterId.toString()
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(558.74, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");

    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date.plusHours(1));
    newEnergyMeasurementWithValue9999J(butterMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Difference+temperature:K,Energy:kWh"
          + "&meters=" + butterMeter.logicalMeterId.toString()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        butterMeter.logicalMeterId.toString(),
        "Difference temperature",
        "K",
        butterMeter.externalId,
        MeasurementControllerTest.BUTTER_METER_DEFINITION.medium,
        asList(
          new MeasurementValueDto(date.toInstant(), 558.74),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 558.74)
        )
      ),
      new MeasurementSeriesDto(
        butterMeter.logicalMeterId.toString(),
        "Energy",
        "kWh",
        butterMeter.externalId,
        MeasurementControllerTest.BUTTER_METER_DEFINITION.medium,
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
    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Difference+temperature"
          + "&meters=" + butterMeter.logicalMeterId.toString()
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.label).isEqualTo(butterMeter.externalId);
  }

  @Test
  public void unknownUnitSuppliedForScaling() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity butterMeter = newButterMeter();
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Difference+temperature:unknownUnit"
          + "&meters=" + butterMeter.logicalMeterId.toString()
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
    PhysicalMeterEntity butterMeter = newButterMeter();
    newDiffTempMeasurementWithValue285_59Celsius(butterMeter, date);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Difference+temperature:kWh"
          + "&meters=" + butterMeter.logicalMeterId.toString()
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
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Difference+temperature:kWh",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'meters' parameter.");
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
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.id);
    newMeasurement(meter, when, "Volume", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume", 55.0, "m³");

    List<MeasurementSeriesDto> list = asTestUser()
      .getList(
        "/measurements?resolution=hour&quantities=Volume"
          + "&meters=" + consumptionMeter.getId()
          + "&after=" + when
          + "&before=" + when.plusHours(2),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(list.size()).isEqualTo(1);

    MeasurementSeriesDto seriesDto = list.get(0);

    assertThat(seriesDto).isEqualTo(
      new MeasurementSeriesDto(
        meter.logicalMeterId.toString(),
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
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.id);
    newMeasurement(meter, when, "Volume", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume", 55.0, "m³");

    MeasurementSeriesDto seriesDto = asTestUser()
      .getList(String.format(
        "/measurements?resolution=hour&quantities=Volume&meters=%s"
          + "&after=%s&before=%s",
        consumptionMeter.getId(),
        when,
        when.plusHours(1)
      ), MeasurementSeriesDto.class).getBody().get(0);

    assertThat(seriesDto).isEqualTo(
      new MeasurementSeriesDto(
        meter.logicalMeterId.toString(),
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
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, when, "Volume", 1.0, "m^3");
    newMeasurement(meter, when.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, when.plusHours(2), "Volume", 5.0, "m^3");

    MeasurementSeriesDto response = asTestUser()
      .getList(String.format(
        "/measurements"
          + "?after=" + when
          + "&before=" + when.plusHours(2)
          + "&quantities=" + Quantity.VOLUME.name
          + "&meters=%s",
        logicalMeter.getId()
      ), MeasurementSeriesDto.class).getBody().get(0);

    ZonedDateTime periodStartHour = when.truncatedTo(ChronoUnit.HOURS);

    assertThat(response.values)
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
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .get(
        "/measurements"
          + "?after=" + after
          + "&before=" + before
          + "&quantities=Floop"
          + "&meters=" + logicalMeter.getId(),
        ErrorMessageDto.class
      );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Floop' for Gas meter");
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return MeterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
  }

  private void newEnergyMeasurementWithValue9999J(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    newMeasurement(meter, created, "Energy", 9999, "J");
  }

  private void newDiffTempMeasurementWithValue285_59Celsius(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    newMeasurement(meter, created, "Difference temperature", 285.59, "°C");
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

  private PhysicalMeterEntity newButterMeterBelongingTo(
    OrganisationEntity organisationEntity,
    ZonedDateTime created
  ) {
    return newPhysicalMeterEntity(organisationEntity, created);
  }

  private PhysicalMeterEntity newButterMeter() {
    return newPhysicalMeterEntity(context().organisationEntity, ZonedDateTime.now());
  }

  private PhysicalMeterEntity newButterMeter(ZonedDateTime created) {
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
      saveMeterDefinition(MeasurementControllerTest.BUTTER_METER_DEFINITION)
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

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asTestSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }
}
