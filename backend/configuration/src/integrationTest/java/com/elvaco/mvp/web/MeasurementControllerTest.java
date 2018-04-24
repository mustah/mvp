package com.elvaco.mvp.web;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
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
    new HashSet<>(
      asList(
        new Quantity(
          "Butter temperature",
          new QuantityPresentationInformation(
            "°C",
            SeriesDisplayMode.READOUT
          )
        ),
        new Quantity(
          "Energy",
          new QuantityPresentationInformation(
            "J",
            SeriesDisplayMode.READOUT
          )
        )
      )
    ),
    false
  );
  private static final double EXPECTED_BUTTER_TEMPERATURE_CELSIUS = 12.44;
  private static final double EXPECTED_BUTTER_TEMPERATURE_KELVIN = 285.59;  // 12.44 Celsius
  private static final Offset<Double> ASSERT_VALUE_EPSILON = within(0.000_000_000_000_1);

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

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
    PhysicalMeterEntity physicalButterMeter = newButterMeter();
    newButterTemperatureMeasurement(physicalButterMeter);

    List<MeasurementDto> measurements = asTestUser()
      .getList("/measurements?meters=" + physicalButterMeter.logicalMeterId, MeasurementDto.class)
      .getBody();

    List<String> quantities = measurements.stream()
      .map(m -> m.quantity)
      .collect(toList());

    assertThat(quantities).contains("Butter temperature");
  }

  @Test
  public void measurementRetrievableById() {
    Long butterTemperatureId = newButterTemperatureMeasurement(
      newButterMeter()
    ).id;

    MeasurementDto measurement = asTestUser()
      .get("/measurements/" + butterTemperatureId, MeasurementDto.class)
      .getBody();

    assertThat(measurement.id).isEqualTo(butterTemperatureId);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementUnitScaled() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter);

    List<MeasurementSeriesDto> measurements = asTestUser()
      .getList(
        "/measurements?quantities=Butter temperature:K"
        + "&meters=" + butterMeter.logicalMeterId,
        MeasurementSeriesDto.class
      )
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Butter temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).values.get(0).value)
      .isEqualTo(EXPECTED_BUTTER_TEMPERATURE_KELVIN);
  }

  @Test
  public void canNotSeeMeasurementsFromMeterBelongingToOtherOrganisation() {
    PhysicalMeterEntity otherOrganisationsMeter = newButterMeterBelongingTo(otherOrganisation);

    newButterTemperatureMeasurement(otherOrganisationsMeter);

    List<MeasurementDto> measurements = asTestUser()
      .getList(
        "/measurements?meters=" + otherOrganisationsMeter.logicalMeterId,
        MeasurementDto.class
      )
      .getBody();

    assertThat(measurements).isEmpty();
  }

  @Test
  public void cannotAccessMeasurementIdOfOtherOrganisationDirectly() {
    Long measurementId = newButterTemperatureMeasurement(
      newButterMeterBelongingTo(otherOrganisation)
    ).id;

    HttpStatus statusCode = asTestUser()
      .get("/measurements/" + measurementId, MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminCanAccessAnyMeasurementDirectly() {
    Long firstOrganisationsMeasurementId = newButterTemperatureMeasurement(
      newButterMeterBelongingTo(otherOrganisation)
    ).id;

    Long secondOrganisationsMeasurementId = newButterTemperatureMeasurement(
      newButterMeterBelongingTo(context().organisationEntity)
    ).id;

    HttpStatus statusCode = asSuperAdmin()
      .get("/measurements/" + firstOrganisationsMeasurementId, MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);

    statusCode = asSuperAdmin()
      .get("/measurements/" + secondOrganisationsMeasurementId, MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void superAdminCanSeeAllMeasurements() {
    PhysicalMeterEntity firstOrganisationsMeter = newButterMeterBelongingTo(otherOrganisation);
    newButterTemperatureMeasurement(
      firstOrganisationsMeter
    );

    PhysicalMeterEntity secondOrganisationsMeter = newButterMeterBelongingTo(context()
      .organisationEntity);
    newButterTemperatureMeasurement(
      secondOrganisationsMeter
    );

    assertThat(getListAsSuperAdmin("/measurements?meters=" + firstOrganisationsMeter
      .logicalMeterId.toString() + "," + secondOrganisationsMeter.logicalMeterId.toString()))
      .hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeter() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(
      butterMeter
    );
    List<String> quantities =
      getListAsSuperAdmin("/measurements?meters=" + butterMeter.logicalMeterId)
        .stream()
        .map(series -> series.quantity)
        .collect(toList());

    assertThat(quantities).containsExactly("Butter temperature");
  }

  @Test
  public void fetchMeasurementsFilteredByQuantity() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter);
    newButterEnergyMeasurement(butterMeter);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
                          + "quantities=Energy"
                          + "&meters=" + butterMeter.logicalMeterId);

    MeasurementSeriesDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Energy");
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityBeforeTime() {

    String date = "1990-01-01T08:00:00Z";
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));
    newButterEnergyMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
                          + "quantities=Butter temperature"
                          + "&meters=" + butterMeter.logicalMeterId
                          + "&before=" + date);

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Butter temperature");
    assertThat(dto.values).hasSize(1);
    assertThat(dto.values.get(0).value).isCloseTo(
      EXPECTED_BUTTER_TEMPERATURE_CELSIUS,
      ASSERT_VALUE_EPSILON
    );
  }

  @Test
  public void fetchMeasurementsForMeterBeforeTime() {
    String date = "1990-01-01T08:00:00Z";
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
                          + "meters=" + butterMeter.logicalMeterId
                          + "&before=" + date);

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Butter temperature");
    assertThat(dto.values).hasSize(1);
    assertThat(dto.values.get(0).value).isCloseTo(
      EXPECTED_BUTTER_TEMPERATURE_CELSIUS,
      ASSERT_VALUE_EPSILON
    );
  }

  @Test
  public void fetchMeasurementsForMeterAfterTime() {
    String date = "1990-01-01T08:00:00Z";
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));

    List<MeasurementSeriesDto> foundSeries =
      getListAsSuperAdmin(
        "/measurements?after=" + date
        + "&meters=" + butterMeter.logicalMeterId.toString()
      );

    assertThat(foundSeries).hasSize(1);
    assertThat(foundSeries.get(0).values).hasSize(1);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    String date = "1990-01-01T08:00:00Z";
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));
    newButterEnergyMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Butter temperature"
                          + "&after=" + date
                          + "&meters=" + butterMeter.logicalMeterId.toString());

    MeasurementSeriesDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Butter temperature");
    assertThat(dto.values).hasSize(1);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTimeWithNonDefaultUnit() {

    String date = "1990-01-01T08:00:00Z";
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).minusDays(1));
    newButterTemperatureMeasurement(butterMeter, ZonedDateTime.parse(date).plusDays(1));

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Butter temperature:K"
                          + "&after=1990-01-01T08:00:00Z"
                          + "&meters=" + butterMeter.logicalMeterId.toString());

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Butter temperature");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.values).hasSize(1);
    assertThat(dto.values.get(0).value).isEqualTo(EXPECTED_BUTTER_TEMPERATURE_KELVIN);
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    MeasurementEntity temperature1 = newButterTemperatureMeasurement(butterMeter);
    MeasurementEntity temperature2 = newButterTemperatureMeasurement(butterMeter);
    MeasurementEntity energy = newButterEnergyMeasurement(butterMeter);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Butter temperature:K,Energy:MJ"
        + "&meters=" + butterMeter.logicalMeterId.toString());

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto("Butter temperature", "K", butterMeter.externalId,
        asList(
          new MeasurementValueDto(
            temperature1.created.toInstant(),
            EXPECTED_BUTTER_TEMPERATURE_KELVIN
          ),
          new MeasurementValueDto(
            temperature2.created.toInstant(),
            EXPECTED_BUTTER_TEMPERATURE_KELVIN
          )
        )
      ),
      new MeasurementSeriesDto("Energy", "MJ", butterMeter.externalId,
        singletonList(new MeasurementValueDto(
            energy.created.toInstant(),
            0.009999
          )
        )
      )
    );
  }

  @Test
  public void measurementSeriesAreLabeledWithMeterExternalId() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter);

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Butter temperature"
        + "&meters=" + butterMeter.logicalMeterId.toString());

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.label).isEqualTo(butterMeter.externalId);
  }

  @Test
  public void unknownUnitSuppliedForScaling() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Butter temperature:unknownUnit"
        + "&meters=" + butterMeter.logicalMeterId.toString(),
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message)
      .isEqualTo("Can not convert to unknown unit 'unknownUnit'");
  }

  @Test
  public void wrongDimensionForQuantitySuppliedForScaling() {
    PhysicalMeterEntity butterMeter = newButterMeter();
    newButterTemperatureMeasurement(butterMeter);

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Butter temperature:kWh"
        + "&meters=" + butterMeter.logicalMeterId.toString(),
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).matches("Can not convert from unit '.*' to 'kWh'");
  }

  @Test
  public void missingMetersParametersReturnsHttp400() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get(
        "/measurements?quantities=Butter temperature:kWh",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'meters' parameter.");
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
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        "average",
        asList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            1.0
          ),
          new MeasurementValueDto(
            Instant.parse("2018-03-06T06:00:00Z"),
            2.0
          )
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
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              2.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-06T06:00:00Z"),
              3.0
            )
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
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        "average",
        singletonList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            4.0
          )
        )
      ),
      new MeasurementSeriesDto(
        Quantity.DIFFERENCE_TEMPERATURE.name,
        Quantity.DIFFERENCE_TEMPERATURE.presentationUnit(),
        "average",
        singletonList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            40.0
          )
        )
      )
    );
  }

  @Test
  public void averageOfOneMeterOneHour() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
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
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          "average",
          singletonList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              4.0
            )
          )
        )));
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
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          "average",
          singletonList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              3.75
            )
          )
        )
      ));
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
  }

  @Test
  public void averageForUndefinedQuantity() {
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

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name, "kW", "average",
          singletonList(new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            40.0
          ))
        )));
  }

  @Test
  public void averageWithDayResolution() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:02Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:03Z"), "Power", 4.0, "W");

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
          Quantity.POWER.name, "W", "average",
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
    newMeasurement(meter, ZonedDateTime.parse("2018-01-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-02-07T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:01Z"), "Power", 4.0, "W");

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
          Quantity.POWER.name, "W", "average",
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

    assertThat(response.get(0)).isEqualTo(
      new MeasurementSeriesDto(
        Quantity.POWER.name,
        "W",
        "average",
        singletonList(new MeasurementValueDto(now.truncatedTo(ChronoUnit.HOURS).toInstant(), 1.0))
      )
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
    ZonedDateTime when = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    LogicalMeterEntity consumptionMeter = newLogicalMeterEntity(

      new MeterDefinition(MeterDefinitionType.UNKNOWN_METER_TYPE, "Consumption of things",
        singleton(
          new Quantity(
            "Volume consumption",
            new QuantityPresentationInformation("m³", SeriesDisplayMode.CONSUMPTION)
          )
        ),
        false
      )
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.id);
    newMeasurement(meter, when, "Volume consumption", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume consumption", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume consumption", 55.0, "m³");

    MeasurementSeriesDto seriesDto = asTestUser()
      .getList(String.format(
        "/measurements?quantities=Volume consumption&meters=%s", consumptionMeter.getId()
      ), MeasurementSeriesDto.class).getBody().get(0);

    assertThat(seriesDto).isEqualTo(
      new MeasurementSeriesDto("Volume consumption", "m³", meter.externalId,
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
    ZonedDateTime when = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    LogicalMeterEntity consumptionMeter = newLogicalMeterEntity(

      new MeterDefinition(MeterDefinitionType.UNKNOWN_METER_TYPE, "Consumption of things",
        singleton(
          new Quantity(
            "Volume consumption",
            new QuantityPresentationInformation("m³", SeriesDisplayMode.CONSUMPTION)
          )
        ),
        false
      )
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(consumptionMeter.id);
    newMeasurement(meter, when, "Volume consumption", 25.0, "m³");
    newMeasurement(meter, when.plusHours(1), "Volume consumption", 35.0, "m³");
    newMeasurement(meter, when.plusHours(2), "Volume consumption", 55.0, "m³");

    MeasurementSeriesDto seriesDto = asTestUser()
      .getList(String.format(
        "/measurements?quantities=Volume consumption&meters=%s"
        + "&after=%s", consumptionMeter.getId(), when.plusHours(1).minusMinutes(1)
      ), MeasurementSeriesDto.class).getBody().get(0);

    assertThat(seriesDto).isEqualTo(
      new MeasurementSeriesDto("Volume consumption", "m³", meter.externalId,
        asList(
          new MeasurementValueDto(when.plusHours(1).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(2).toInstant(), 20.0)
        )
      )
    );
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return meterDefinitionJpaRepository.save(MeterDefinitionMapper.toEntity(meterDefinition));
  }

  private MeasurementEntity newButterEnergyMeasurement(
    PhysicalMeterEntity meter
  ) {
    return newButterEnergyMeasurement(meter, ZonedDateTime.now());
  }

  private MeasurementEntity newButterEnergyMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when
  ) {
    return newMeasurement(meter, when, "Energy", 9999, "J");
  }

  private MeasurementEntity newButterTemperatureMeasurement(
    PhysicalMeterEntity meter
  ) {
    return newButterTemperatureMeasurement(meter, ZonedDateTime.now());
  }

  private MeasurementEntity newButterTemperatureMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when
  ) {
    return newMeasurement(meter, when, "Butter temperature",
      EXPECTED_BUTTER_TEMPERATURE_CELSIUS, "°C"
    );
  }

  private MeasurementEntity newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when,
    String quantity,
    double value,
    String unit
  ) {
    return measurementJpaRepository.save(new MeasurementEntity(
      when,
      quantity,
      value,
      unit,
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

  private PhysicalMeterEntity newButterMeterBelongingTo(OrganisationEntity organisationEntity) {
    return newPhysicalMeterEntity(
      BUTTER_METER_DEFINITION,
      organisationEntity
    );
  }

  private PhysicalMeterEntity newButterMeter() {
    return newPhysicalMeterEntity(
      BUTTER_METER_DEFINITION
    );
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(MeterDefinition meterDefinition) {
    return newPhysicalMeterEntity(meterDefinition, context().organisationEntity);
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(
    MeterDefinition meterDefinition,
    OrganisationEntity organisationEntity
  ) {
    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);
    UUID logicalMeterId = randomUUID();
    logicalMeterJpaRepository.save(new LogicalMeterEntity(
      logicalMeterId,
      logicalMeterId.toString(),
      organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
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
      emptySet()
    ));
  }

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }
}
