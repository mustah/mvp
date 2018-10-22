package com.elvaco.mvp.web;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
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
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerAverageTest extends IntegrationTest {

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
    ZonedDateTime after = ZonedDateTime.parse("2018-03-06T05:00:00Z");

    LogicalMeterEntity logicalMeter1 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, after, "Energy", 1.0, "kWh");
    newMeasurement(meter1, after.plusHours(1), "Energy", 12.0, "kWh");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(MeterDefinition
      .DISTRICT_HEATING_METER);
    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, after, "Energy", 3.0, "kWh");
    newMeasurement(meter2, after.plusHours(1), "Energy", 8.0, "kWh");

    ResponseEntity<List<MeasurementSeriesDto>> response = asTestUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + after
          + "&before=" + after.plusHours(1)
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
            new MeasurementValueDto(Instant.parse("2018-03-06T05:00:00Z"), 8.0),
            new MeasurementValueDto(Instant.parse("2018-03-06T06:00:00Z"), null)
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

      "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T05:59:59.999Z"
        + "&quantities={powerQuantity},{diffTempQuantity}"
        + "&meters={meterId}"
        + "&resolution=hour",
      MeasurementSeriesDto.class,
      Quantity.POWER.name,
      Quantity.DIFFERENCE_TEMPERATURE.name,
      logicalMeter.id.toString()
    );

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

        "/measurements/average"
          + "?after={after}"
          + "&before={before}"
          + "&quantities={powerQuantity}"
          + "&meters={meterId}&resolution=hour",
        MeasurementSeriesDto.class,
        "2018-03-06T03:00:00.000-02:00",
        "2018-03-06T06:59:59.999+01:00",
        Quantity.POWER.name,
        logicalMeter.id.toString()
      );

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
    assertThat(response.getBody().message).isEqualTo("Invalid quantity 'SomeUnknownQuantity' for "
      + "District heating meter");
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
      .getList(
        "/measurements/average"
          + "?after={now}"
          + "&quantities={powerQuantity}:W"
          + "&meters={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        now,
        Quantity.POWER.name,
        logicalMeter.getId()
      ).getBody();

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
  public void findsAverageConsumptionForGasMeters() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:00:00Z");
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
          + "&before=" + after.plusHours(3)
          + "&quantities=" + Quantity.VOLUME.name
          + "&meters=" + logicalMeter.getId(),
        MeasurementSeriesDto.class
      ).getBody().get(0);

    ZonedDateTime periodStartHour = after.truncatedTo(ChronoUnit.HOURS);

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
        ),
        new MeasurementValueDto(
          periodStartHour.plusHours(3).toInstant(),
          null
        )
      );
  }

  @Test
  public void averageForTwoMetersAndTwoQuantitiesWhereOneQuantityIsNotPresentOnOneMeter() {
    ZonedDateTime after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    ZonedDateTime before = ZonedDateTime.parse("2018-02-01T04:59:10Z");

    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(GAS_METER);

    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after, "Volume", 1.0, "m^3");
    newMeasurement(meter, after.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, after.plusHours(2), "Volume", 5.0, "m^3");

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .get(String.format(
        "/measurements/average"
          + "?after=" + after
          + "&before=" + before
          + "&quantities=Volume,Power"
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
          + "&meters=" + logicalMeter.getId(),
        ErrorMessageDto.class
      );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Flarbb' for Gas "
      + "meter");
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
