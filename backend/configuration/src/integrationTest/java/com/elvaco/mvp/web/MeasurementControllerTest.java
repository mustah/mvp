package com.elvaco.mvp.web;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
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
import static java.util.Collections.singletonList;
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
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private LocationJpaRepository locationJpaRepository;

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
  public void measurementsRetrievableAtEndpoint() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity physicalButterMeter = newButterMeter(date);
    newButterTemperatureMeasurement(
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
    newButterTemperatureMeasurement(butterMeter, date);

    List<MeasurementSeriesDto> measurements = asTestUser()
      .getList(
        "/measurements?quantities=Difference temperature:K"
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

    newButterTemperatureMeasurement(otherOrganisationsMeter, date);

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
    newButterTemperatureMeasurement(
      firstOrganisationsMeter,
      date
    );

    PhysicalMeterEntity secondOrganisationsMeter = newButterMeterBelongingTo(
      context().organisationEntity,
      date
    );
    newButterTemperatureMeasurement(
      secondOrganisationsMeter,
      date
    );

    assertThat(getListAsSuperAdmin(
      "/measurements?meters=" + firstOrganisationsMeter.logicalMeterId.toString()
      + "," + secondOrganisationsMeter.logicalMeterId.toString()
      + "&quantities=Difference temperature"
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
    newButterTemperatureMeasurement(butterMeter, date.minusHours(1));
    newButterTemperatureMeasurement(butterMeter, date.plusHours(1));
    newButterEnergyMeasurement(butterMeter, date.minusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
                          + "quantities=Difference temperature"
                          + "&meters=" + butterMeter.logicalMeterId
                          + "&after=" + date
                          + "&before=" + date.plusHours(2)
                          + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.values).hasSize(1);
    assertThat(dto.values.get(0).value).isEqualTo(558.74, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterInPeriod() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");
    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newButterTemperatureMeasurement(butterMeter, date.minusHours(1));
    newButterTemperatureMeasurement(butterMeter, date.plusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
                          + "meters=" + butterMeter.logicalMeterId
                          + "&after=" + date
                          + "&before=" + date.plusHours(2)
                          + "&resolution=hour");

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        butterMeter.logicalMeterId.toString(),
        "Difference temperature",
        "K",
        butterMeter.externalId,
        MeasurementControllerTest.BUTTER_METER_DEFINITION.medium,
        singletonList(new MeasurementValueDto(date.plusHours(1).toInstant(), 558.74))
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
    newButterTemperatureMeasurement(butterMeter, date.minusHours(1));
    newButterTemperatureMeasurement(butterMeter, date.plusHours(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Difference temperature:K"
                          + "&after=" + date
                          + "&before=" + date.plusHours(2)
                          + "&meters=" + butterMeter.logicalMeterId.toString()
                          + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.values).hasSize(1);
    assertThat(dto.values.get(0).value).isEqualTo(558.74, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    ZonedDateTime date = ZonedDateTime.parse("1990-01-01T08:00:00Z");

    PhysicalMeterEntity butterMeter = newButterMeter(date);
    newButterTemperatureMeasurement(butterMeter, date);
    newButterTemperatureMeasurement(butterMeter, date.plusHours(1));
    newButterEnergyMeasurement(butterMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Difference temperature:K,Energy:kWh"
        + "&meters=" + butterMeter.logicalMeterId.toString()
        + "&resolution=hour"
        + "&after=" + date
        + "&before=" + date.plusHours(2));

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
    newButterTemperatureMeasurement(butterMeter, date);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Difference temperature"
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
    newButterTemperatureMeasurement(butterMeter, date);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Difference temperature:unknownUnit"
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
    newButterTemperatureMeasurement(butterMeter, date);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Difference temperature:kWh"
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
        "/measurements?quantities=Difference temperature:kWh",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'meters' parameter.");
  }

  @Test
  public void averagesForDifferentCitiesHaveUniqueIds() {
    LocationBuilder locationBuilder = new LocationBuilder();
    locationBuilder
      .address("street 1")
      .city("stockholm");

    LogicalMeterEntity stockholmSweden = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      locationBuilder.country("sweden").build()
    );
    LogicalMeterEntity stockholmEngland = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      locationBuilder.country("england").build()
    );

    PhysicalMeterEntity swedenMeter = newPhysicalMeterEntity(stockholmSweden.id);
    newMeasurement(swedenMeter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(swedenMeter, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");
    PhysicalMeterEntity englandMeter = newPhysicalMeterEntity(stockholmEngland.id);
    newMeasurement(englandMeter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(englandMeter, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      "/measurements/cities"
      + "?after=2018-03-06T05:00:00.000Z"
      + "&before=2018-03-06T06:59:59.999Z"
      + "&quantities=" + Quantity.POWER.name
      + "&city=sweden,stockholm"
      + "&city=england,stockholm"
      + "&resolution=hour",
      MeasurementSeriesDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting("id")
      .containsExactlyInAnyOrder(
        "city-sweden,stockholm-Power",
        "city-england,stockholm-Power"
      );
  }

  @Test
  public void averageOfOneMeterTwoHours() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);

    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(new MeasurementSeriesDto(
        "average-Power",
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        "average",
        asList(
          new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-03-06T06:00:00Z"), 2.0)
        )
      )));
  }

  @Test
  public void averageOfTwoMetersTwoHours() {
    LogicalMeterEntity logicalMeter1 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 3.0, "W");
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        String.join(",", asList(logicalMeter1.id.toString(), logicalMeter2.id.toString()))
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Power",
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          "average",
          asList(
            new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 2.0),
            new MeasurementValueDto(Instant.parse("2018-03-06T06:00:00Z"), 3.0)
          )
        )));
  }

  @Test
  public void averageOfConsumptionSeries() {
    LogicalMeterEntity logicalMeter1 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T05:00:00Z"), "Energy", 1.0, "kWh");
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T06:00:00Z"), "Energy", 12.0, "kWh");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T05:00:00Z"), "Energy", 3.0, "kWh");
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T06:00:00Z"), "Energy", 8.0, "kWh");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:59:59.999Z"
        + "&quantities=" + Quantity.ENERGY.name + ":kWh"
        + "&meters=%s"
        + "&resolution=hour",
        String.join(",", asList(logicalMeter1.id.toString(), logicalMeter2.id.toString()))
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Energy",
          Quantity.ENERGY.name,
          Quantity.ENERGY.presentationUnit(),
          "average",
          asList(
            new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), null),
            new MeasurementValueDto(Instant.parse("2018-03-06T06:00:00Z"), 8.0)
          )
        )));
  }

  @Test
  public void averageForMultipleQuantities() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:02Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:03Z"), "Power", 6.0, "W");

    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:01Z"),
      "Difference temperature",
      20.0,
      "K"
    );
    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:02Z"),
      "Difference temperature",
      40.0,
      "K"
    );
    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:03Z"),
      "Difference temperature",
      60.0,
      "K"
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name + "," + Quantity.DIFFERENCE_TEMPERATURE.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "average-Power",
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        "average",
        singletonList(new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 4.0))
      ),
      new MeasurementSeriesDto(
        "average-Difference temperature",
        Quantity.DIFFERENCE_TEMPERATURE.name,
        Quantity.DIFFERENCE_TEMPERATURE.presentationUnit(),
        "average",
        singletonList(new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 40.0))
      )
    );
  }

  @Test
  public void averageOfOneMeterOneHour() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:02Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:03Z"), "Power", 6.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Power",
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          "average",
          singletonList(new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 4.0))
        )));
  }

  @Test
  public void oneCityAverage() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    Location storaGatan1 = locationBuilder.build();

    Location storaGatan2 = locationBuilder.address("stora gatan 2").build();

    LogicalMeterEntity meter1 = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      storaGatan1
    );
    PhysicalMeterEntity physical1 = newPhysicalMeterEntity(meter1.id);

    LogicalMeterEntity meter2 = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      storaGatan2
    );
    PhysicalMeterEntity physical2 = newPhysicalMeterEntity(meter2.id);

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(physical1, start, "Power", 1.0, "W");
    newMeasurement(physical1, start.plusHours(1), "Power", 2.0, "W");

    newMeasurement(physical2, start, "Power", 3.0, "W");
    newMeasurement(physical2, start.plusHours(1), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(
        "/measurements/cities"
        + "?after=" + start
        + "&before=" + start.plusHours(1)
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&city=sweden,stockholm"
        + "&meters=123"
        + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      new MeasurementSeriesDto(
        "city-sweden,stockholm-Power",
        Quantity.POWER.name,
        "W",
        "sweden,stockholm",
        "stockholm",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 2.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 3.0)
        )
      )
    );
  }

  @Test
  public void cityAverageOnlyIncludesRequestedCity() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    PhysicalMeterEntity physical1 = newPhysicalMeterEntity(
      newLogicalMeterEntityWithLocation(
        MeterDefinition.DISTRICT_HEATING_METER,
        locationBuilder.build()
      ).id
    );

    PhysicalMeterEntity physical2 = newPhysicalMeterEntity(
      newLogicalMeterEntityWithLocation(
        MeterDefinition.DISTRICT_HEATING_METER,
        locationBuilder.address("stora gatan 2").build()
      ).id
    );

    PhysicalMeterEntity physicalIrrelevant = newPhysicalMeterEntity(
      newLogicalMeterEntityWithLocation(
        MeterDefinition.DISTRICT_HEATING_METER,
        locationBuilder.city("båstad").build()
      ).id
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(physical1, start, "Power", 1.0, "W");
    newMeasurement(physical1, start.plusHours(1), "Power", 2.0, "W");

    newMeasurement(physical2, start, "Power", 3.0, "W");
    newMeasurement(physical2, start.plusHours(1), "Power", 4.0, "W");

    newMeasurement(physicalIrrelevant, start, "Power", 10.0, "W");
    newMeasurement(physicalIrrelevant, start.plusHours(1), "Power", 10.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(
        "/measurements/cities"
        + "?after=" + start
        + "&before=" + start.plusHours(1)
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&city=sweden,stockholm"
        + "&meters=123"
        + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
      .extracting("city")
      .hasSize(1)
      .allMatch((city) -> "stockholm".equals(city));
  }

  @Test
  public void twoCityAverages() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    LogicalMeterEntity meter1 = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      locationBuilder.build()
    );
    PhysicalMeterEntity physical1 = newPhysicalMeterEntity(meter1.id);

    LogicalMeterEntity meter3 = newLogicalMeterEntityWithLocation(
      MeterDefinition.DISTRICT_HEATING_METER,
      locationBuilder.city("båstad").build()
    );
    PhysicalMeterEntity physical3 = newPhysicalMeterEntity(meter3.id);

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(physical1, start, "Power", 1.0, "W");
    newMeasurement(physical1, start.plusHours(1), "Power", 2.0, "W");

    newMeasurement(physical3, start, "Power", 10.0, "W");
    newMeasurement(physical3, start.plusHours(1), "Power", 10.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(
        "/measurements/cities"
        + "?after=" + start
        + "&before=" + start.plusHours(1)
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&city=sweden,stockholm"
        + "&city=sweden,båstad"
        + "&meters=123"
        + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "city-sweden,stockholm-Power",
        Quantity.POWER.name,
        "W",
        "sweden,stockholm",
        "stockholm",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 2.0)
        )
      ),
      new MeasurementSeriesDto(
        "city-sweden,båstad-Power",
        Quantity.POWER.name,
        "W",
        "sweden,båstad",
        "båstad",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 10.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 10.0)
        )
      )
    );
  }

  @Test
  public void timeZoneInformationIsConsidered() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T03:00:01-02:00"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T04:01:01-01:00"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:02:01Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T06:03:01+01:00"), "Power", 8.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);
    ResponseEntity<List<MeasurementSeriesDto>> responseForNonZuluRequest =
      asTestUser().getList(
        String.format(
          "/measurements/average"
          + "?after=2018-03-06T03:00:00.000-02:00"
          + "&before=2018-03-06T06:59:59.999+01:00"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s&resolution=hour",
          logicalMeter.id.toString()
        ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(
          Instant.parse("2018-03-06T05:00:00Z"),
          3.75
        )
      );
    assertThat(response.getBody()).isEqualTo(responseForNonZuluRequest.getBody());
  }

  @Test
  public void averageForNoPhysicalMeters() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message)
      .contains("No physical meters connected to logical meter");
  }

  @Test
  public void averageForUnknownQuantity() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=SomeUnknownQuantity"
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).contains(
      "Invalid quantity 'SomeUnknownQuantity' for District heating meter");
  }

  @Test
  public void averageWithNoMatchingMeasurements() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Power",
          Quantity.POWER.name, "W",
          "average",
          singletonList(new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            null
          ))
        )));
  }

  @Test
  public void allowsOverridingDefinitionsPresentationUnit() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 40000.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name + ":kW"
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
      .extracting("quantity")
      .containsExactly(Quantity.POWER.name);

    assertThat(response.getBody())
      .extracting("unit")
      .containsExactly("kW");
  }

  @Test
  public void averageWithDayResolution() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T00:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T00:00:02Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T00:00:03Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=day",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Power",
          Quantity.POWER.name,
          "W",
          "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T00:00:00Z"),
              1.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-07T00:00:00Z"),
              3.0
            )
          )
        )
      ));
  }

  @Test
  public void averageWithMonthResolution() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-01-01T00:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-02-01T00:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-01T00:00:01Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=2018-01-01T05:00:00.000Z"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=month",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          "average-Power",
          Quantity.POWER.name,
          "W",
          "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-01-01T00:00:00Z"),
              1.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-02-01T00:00:00Z"),
              2.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-01T00:00:00Z"),
              4.0
            )
          )
        )
      ));
  }

  @Test
  public void invalidAverageParameterValuesReturnsHttp400() {

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=thisIsNotAValidTimestamp"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=month",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'after' timestamp: 'thisIsNotAValidTimestamp'.");

    response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-07T12:32:05.999Z"
        + "&before=thisIsNotAValidTimestamp"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=month",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'before' timestamp: 'thisIsNotAValidTimestamp'.");

    response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-07T12:32:05.999Z"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=month",
        "NotAValidUUID"
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Invalid 'meters' list: 'NotAValidUUID'.");

    response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-07T12:32:05.999Z"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=NotAValidResolution",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'resolution' parameter: 'NotAValidResolution'.");
  }

  @Test
  public void missingParametersReturnsHttp400() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?to=2018-03-07T12:32:05.999Z"
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=hour",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Missing 'after' parameter.");

    response = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=2018-03-07T12:32:05.999Z"
        + "&before=2018-03-07T12:32:05.999Z"
        + "&meters=%s"
        + "&resolution=hour",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Missing 'quantities' parameter.");
  }

  @Test
  public void toTimestampDefaultsToNow() {
    ZonedDateTime now = ZonedDateTime.now();
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, now, "Power", 1.0, "W");

    List<MeasurementSeriesDto> response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=" + now.toString()
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s"
        + "&resolution=hour",
        logicalMeter.getId()
      ), MeasurementSeriesDto.class).getBody();

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(now.truncatedTo(ChronoUnit.HOURS).toInstant(), 1.0)
      );
  }

  @Test
  public void resolutionDefaultsToHourForPeriodLessThanADay() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T23:59:10Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after.plusHours(2), "Power", 1.0, "W");

    MeasurementSeriesDto response = asTestUser()
      .getList(String.format(
        "/measurements/average"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=" + Quantity.POWER.name + ":W"
        + "&meters=%s",
        logicalMeter.getId()
      ), MeasurementSeriesDto.class).getBody().get(0);
    assertThat(response.values).hasSize(23);
  }

  @Test
  public void consumptionSeriesIsDisplayedWithConsumptionValues() {
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
        + "&before=" + when.plusHours(3),
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
          new MeasurementValueDto(when.toInstant(), null),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(2).toInstant(), 20.0)
        )
      )
    );
  }

  @Test
  public void consumptionIsIncludedForFirstValueInPeriodWhenPreviousValueExists() {
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
        when.plusHours(1),
        when.plusHours(3)
      ), MeasurementSeriesDto.class).getBody().get(0);

    assertThat(seriesDto).isEqualTo(
      new MeasurementSeriesDto(
        meter.logicalMeterId.toString(),
        "Volume",
        "m³",
        meter.externalId,
        consumptionMeter.meterDefinition.medium,
        asList(
          new MeasurementValueDto(when.plusHours(1).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(2).toInstant(), 20.0)
        )
      )
    );
  }

  @Test
  public void findsConsumptionForGasMeters() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:00:00Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");

    MeasurementSeriesDto response = asTestUser()
      .getList(String.format(
        "/measurements"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=" + Quantity.VOLUME.name
        + "&meters=%s",
        logicalMeter.getId()
      ), MeasurementSeriesDto.class).getBody().get(0);

    ZonedDateTime periodStartHour = after.truncatedTo(ChronoUnit.HOURS);

    assertThat(response.values)
      .containsExactly(
        new MeasurementValueDto(
          periodStartHour.toInstant(),
          null
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(1).toInstant(),
          1.0
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(2).toInstant(),
          3.0
        )
      );
  }

  @Test
  public void findsAverageConsumptionForGasMeters() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:00:00Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");

    MeasurementSeriesDto response = asTestUser()
      .getList(
        "/measurements/average"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=" + Quantity.VOLUME.name
        + "&meters=" + logicalMeter.getId(),
        MeasurementSeriesDto.class
      ).getBody().get(0);

    ZonedDateTime periodStartHour = after.truncatedTo(ChronoUnit.HOURS);

    assertThat(response.values)
      .containsExactly(
        new MeasurementValueDto(
          periodStartHour.toInstant(),
          null
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(1).toInstant(),
          1.0
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(2).toInstant(),
          3.0
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(3).toInstant(),
          null
        )
      );
  }

  @Test
  public void measurementsForNonPresentQuantity() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:59:10Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .get(
        "/measurements"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=Floop"
        + "&meters=" + logicalMeter.getId(), ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Floop' for Gas meter");
  }

  @Test
  public void averageForTwoMetersAndTwoQuantitiesWhereOneQuantityIsNotPresentOnOneMeter() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:59:10Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .get(String.format(
        "/measurements/average"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=" + Quantity.VOLUME.name + ",Power"
        + "&meters=%s",
        logicalMeter.getId()
      ), ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Power' for Gas meter");
  }

  @Test
  public void averageForTwoQuantitiesWhereOneIsNotPresent() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:59:10Z");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      GAS_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .get(
        "/measurements/average"
        + "?after=" + after
        + "&before=" + before
        + "&quantities=" + Quantity.VOLUME.name + ",Flarbb"
        + "&meters=" + logicalMeter.getId(), ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message)
      .contains("Invalid quantity 'Flarbb' for Gas meter");
  }

  @Test
  public void pagedMeasurements() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z[UTC]");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T06:00:00Z[UTC]");
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(GAS_METER);
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);

    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");
    newMeasurement(meter, after.plusHours(3), "Volume", 6.0, "m^3");
    newMeasurement(meter, after.plusHours(4), "Volume", 7.0, "m^3");

    org.springframework.data.domain.Page<MeasurementDto> response = asTestUser()
      .getPage(String.format(
        "/measurements/paged/?after=%s&before=%s&logicalMeterId=%s&size=2&sort=created,desc",
        after, before, logicalMeter.getId()
      ), MeasurementDto.class);

    assertThat(response.getTotalElements()).isEqualTo(5);
    assertThat(response.getTotalPages()).isEqualTo(3);

    List<MeasurementDto> content = response.getContent();
    assertThat(content.size()).isEqualTo(2);

    assertThat(content.get(0)).isEqualTo(new MeasurementDto(
      "Volume",
      7.0,
      "m³",
      after.plusHours(4)
    ));

    assertThat(content.get(1)).isEqualTo(new MeasurementDto(
      "Volume",
      6.0,
      "m³",
      after.plusHours(3)
    ));
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

  private MeasurementEntity newButterEnergyMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created
  ) {
    return newMeasurement(meter, created, "Energy", 9999, "J");
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

  private LogicalMeterEntity newLogicalMeterEntityWithLocation(
    MeterDefinition meterDefinition,
    Location location
  ) {
    UUID uuid = randomUUID();

    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);

    LogicalMeterEntity meter = logicalMeterJpaRepository.save(new LogicalMeterEntity(
      uuid,
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
    ));

    locationJpaRepository.save(new LocationEntity(
      meter.id,
      location.getCountry(),
      location.getCity(),
      location.getAddress()
    ));

    return meter;
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
