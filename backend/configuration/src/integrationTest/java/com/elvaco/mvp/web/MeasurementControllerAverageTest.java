package com.elvaco.mvp.web;

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

  private static final String SERIES_ID_AVERAGE_POWER = "average-Power";
  private static final String SERIES_ID_AVERAGE_ENERGY = "average-Energy";
  private static final String AVERAGE = "average";
  private static final String SERIES_ID_AVERAGE_DIFF_TEMP = "average-Difference temperature";

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
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Power", 1.0, "W");
    newMeasurement(meter, date.plusHours(1), "Power", 2.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(new MeasurementSeriesDto(
        SERIES_ID_AVERAGE_POWER,
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        AVERAGE,
        asList(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 2.0)
        )
      )));
  }

  @Test
  public void averageOfTwoMetersTwoHours() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter1 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, date, "Power", 1.0, "W");
    newMeasurement(meter1, date.plusHours(1), "Power", 2.0, "W");

    var logicalMeter2 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, date, "Power", 3.0, "W");
    newMeasurement(meter2, date.plusHours(1), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        String.join(",", asList(logicalMeter1.id.toString(), logicalMeter2.id.toString()))
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          AVERAGE,
          asList(
            new MeasurementValueDto(date.toInstant(), 2.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), 3.0)
          )
        )));
  }

  @Test
  public void averageOfConsumptionSeries() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");

    var logicalMeter1 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, date, "Energy", 1.0, "kWh");
    newMeasurement(meter1, date.plusHours(1), "Energy", 12.0, "kWh");

    var logicalMeter2 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, date, "Energy", 3.0, "kWh");
    newMeasurement(meter2, date.plusHours(1), "Energy", 8.0, "kWh");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantities=" + Quantity.ENERGY.name + ":kWh"
          + "&meters=%s"
          + "&resolution=hour",
        String.join(",", asList(logicalMeter1.id.toString(), logicalMeter2.id.toString()))
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_ENERGY,
          Quantity.ENERGY.name,
          Quantity.ENERGY.presentationUnit(),
          AVERAGE,
          asList(
            new MeasurementValueDto(date.toInstant(), 8.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          )
        )));
  }

  @Test
  public void averageForMultipleQuantities() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");

    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Power", 2.0, "W");
    newMeasurement(meter, date.plusSeconds(2), "Power", 4.0, "W");

    newMeasurement(meter, date, "Difference temperature", 20.0, "K");
    newMeasurement(meter, date.plusSeconds(2), "Difference temperature", 40.0, "K");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(

      "/measurements/average"
        + "?after=" + date
        + "&before=" + date.plusMinutes(1)
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
        SERIES_ID_AVERAGE_POWER,
        Quantity.POWER.name,
        Quantity.POWER.presentationUnit(),
        AVERAGE,
        singletonList(new MeasurementValueDto(date.toInstant(), 2.0))
      ),
      new MeasurementSeriesDto(
        SERIES_ID_AVERAGE_DIFF_TEMP,
        Quantity.DIFFERENCE_TEMPERATURE.name,
        Quantity.DIFFERENCE_TEMPERATURE.presentationUnit(),
        AVERAGE,
        singletonList(new MeasurementValueDto(date.toInstant(), 20.0))
      )
    );
  }

  @Test
  public void averageOfOneMeterOneHourShoulOnlyIncludeValueAtIntervalStart() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date.plusSeconds(1), "Power", 2.0, "W");
    newMeasurement(meter, date.plusSeconds(2), "Power", 4.0, "W");
    newMeasurement(meter, date.plusSeconds(3), "Power", 6.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name,
          Quantity.POWER.presentationUnit(),
          AVERAGE,
          singletonList(new MeasurementValueDto(date.toInstant(), null))
        )));
  }

  @Test
  public void timeZoneInformationIsConsidered() {
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:00:00.000Z"
        + "&quantities={quantiy}"
        + "&meters={meterId}"
        + "&resolution=hour",
      MeasurementSeriesDto.class,
      Quantity.POWER.name,
      logicalMeter.id.toString()
    );

    ResponseEntity<List<MeasurementSeriesDto>> responseForNonZuluRequest =
      asUser().getList(
        "/measurements/average"
          + "?after={after}"
          + "&before={before}"
          + "&quantities={quantiy}"
          + "&meters={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        "2018-03-06T03:00:00.000-02:00",
        "2018-03-06T07:00:00.000+01:00",
        Quantity.POWER.name,
        logicalMeter.id.toString()
      );

    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name,
          "W",
          AVERAGE,
          asList(
            new MeasurementValueDto(
              ZonedDateTime.parse("2018-03-06T05:00:00.000Z").toInstant(),
              null
            ),
            new MeasurementValueDto(
              ZonedDateTime.parse("2018-03-06T06:00:00.000Z").toInstant(),
              null
            )
          )
        )
      ));
    assertThat(response.getBody()).isEqualTo(responseForNonZuluRequest.getBody());
  }

  @Test
  public void averageForNoPhysicalMetersReturnsNotFound() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
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
  public void averageForUnknownQuantityReturnsBadRequest() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
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
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name, "W",
          AVERAGE,
          singletonList(new MeasurementValueDto(date.toInstant(), null))
        )));
  }

  @Test
  public void allowsOverridingDefinitionsPresentationUnit() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Power", 40000.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
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
  public void averageWithDayResolutionIncludesFromAndToDatesButOnlyValuesAtResolution() {
    var date = ZonedDateTime.parse("2018-03-06T00:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date.plusSeconds(1), "Power", 1.0, "W");
    newMeasurement(meter, date.plusDays(1).plusSeconds(2), "Power", 2.0, "W");
    newMeasurement(meter, date.plusDays(1).plusSeconds(3), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.minusDays(1)
          + "&before=" + date.plusDays(2).minusNanos(1)
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=day",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name,
          "W",
          AVERAGE,
          asList(
            new MeasurementValueDto(date.minusDays(1).toInstant(), null),
            new MeasurementValueDto(date.toInstant(), null),
            new MeasurementValueDto(date.plusDays(1).toInstant(), null)
          )
        )
      ));
  }

  @Test
  public void averageWithMonthResolution() {
    var date = ZonedDateTime.parse("2018-01-01T00:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Power", 1.0, "W");
    newMeasurement(meter, date.plusMonths(1), "Power", 2.0, "W");
    newMeasurement(meter, date.plusMonths(2), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.plusHours(5)
          + "&before=" + date.plusMonths(2)
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=month",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          Quantity.POWER.name,
          "W",
          AVERAGE,
          asList(
            new MeasurementValueDto(date.toInstant(), 1.0),
            new MeasurementValueDto(date.plusMonths(1).toInstant(), 2.0),
            new MeasurementValueDto(date.plusMonths(2).toInstant(), 4.0)
          )
        )
      ));
  }

  @Test
  public void invalidAverageParameterValuesReturnsHttp400() {
    ResponseEntity<ErrorMessageDto> response = asUser()
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

    response = asUser()
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

    response = asUser()
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

    response = asUser()
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
    ResponseEntity<ErrorMessageDto> response = asUser()
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

    response = asUser()
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
    ZonedDateTime date = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0);
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Power", 1.0, "W");

    List<MeasurementSeriesDto> response = asUser()
      .getList(
        "/measurements/average"
          + "?after={now}"
          + "&quantities={powerQuantity}:W"
          + "&meters={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        date,
        Quantity.POWER.name,
        logicalMeter.getId()
      ).getBody();

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(date.toInstant(), 1.0)
      );
  }

  @Test
  public void resolutionDefaultsToHourForPeriodLessThanADay() {
    var after = ZonedDateTime.parse("2018-02-01T01:12:00Z");
    var before = ZonedDateTime.parse("2018-02-01T23:59:10Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, after.plusHours(2), "Power", 1.0, "W");

    MeasurementSeriesDto response = asUser()
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
    var date = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    var logicalMeter = newLogicalMeterEntity(GAS_METER);
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Volume", 1.0, "m^3");
    newMeasurement(meter, date.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, date.plusHours(2), "Volume", 5.0, "m^3");

    MeasurementSeriesDto response = asUser()
      .getList(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(3)
          + "&quantities=" + Quantity.VOLUME.name
          + "&meters=" + logicalMeter.getId(),
        MeasurementSeriesDto.class
      ).getBody().get(0);

    ZonedDateTime periodStartHour = date.truncatedTo(ChronoUnit.HOURS);

    assertThat(response.values)
      .containsExactly(
        new MeasurementValueDto(periodStartHour.toInstant(), 1.0),
        new MeasurementValueDto(periodStartHour.plusHours(1).toInstant(), 3.0),
        new MeasurementValueDto(periodStartHour.plusHours(2).toInstant(), null),
        new MeasurementValueDto(periodStartHour.plusHours(3).toInstant(), null)
      );
  }

  @Test
  public void averageForTwoMetersAndTwoQuantitiesWhereOneQuantityIsNotPresentOnOneMeter() {
    var date = ZonedDateTime.parse("2018-02-01T01:12:00Z");

    var logicalMeter = newLogicalMeterEntity(GAS_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Volume", 1.0, "m^3");
    newMeasurement(meter, date.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, date.plusHours(2), "Volume", 5.0, "m^3");

    ResponseEntity<ErrorMessageDto> responseEntity = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(4)
          + "&quantities=Volume,Power"
          + "&meters=%s",
        logicalMeter.getId()
      ), ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody().message).contains("Invalid quantity 'Power' for Gas meter");
  }

  @Test
  public void averageForTwoQuantitiesWhereOneIsNotPresent() {
    var date = ZonedDateTime.parse("2018-02-01T01:12:00Z");

    var logicalMeter = newLogicalMeterEntity(GAS_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, date, "Volume", 1.0, "m^3");
    newMeasurement(meter, date.plusHours(1), "Volume", 2.0, "m^3");
    newMeasurement(meter, date.plusHours(2), "Volume", 5.0, "m^3");

    ResponseEntity<ErrorMessageDto> responseEntity = asUser()
      .get(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(4)
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
