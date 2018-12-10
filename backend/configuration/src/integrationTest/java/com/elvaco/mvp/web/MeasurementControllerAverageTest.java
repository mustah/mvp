package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.LocationEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
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

  @Autowired
  private QuantityEntityMapper quantityEntityMapper;

  @Autowired
  private MeterDefinitionEntityMapper meterDefinitionEntityMapper;

  @Autowired
  private QuantityProvider quantityProvider;

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

  @Test
  public void oneMeter_OnlyIncludesAskedForMeters() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");

    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var physicalMeter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(physicalMeter, date, "Power", 1.0);
    newMeasurement(physicalMeter, date.plusHours(1), "Power", 2.0);

    var logicalMeterId = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER)
      .getLogicalMeterId();
    var uninterestingPhysicalMeter = newPhysicalMeterEntity(logicalMeterId);
    newMeasurement(uninterestingPhysicalMeter, date, "Power", 99.0);
    newMeasurement(uninterestingPhysicalMeter, date.plusHours(1), "Power", 100.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
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
  public void oneMeter_TwoHours() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Power", 1.0);
    newMeasurement(meter, date.plusHours(1), "Power", 2.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
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
  public void twoMeters_TwoHours() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter1 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter1 = newPhysicalMeterEntity(logicalMeter1.getLogicalMeterId());
    newMeasurement(meter1, date, "Power", 1.0);
    newMeasurement(meter1, date.plusHours(1), "Power", 2.0);

    var logicalMeter2 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter2 = newPhysicalMeterEntity(logicalMeter2.getLogicalMeterId());
    newMeasurement(meter2, date, "Power", 3.0);
    newMeasurement(meter2, date.plusHours(1), "Power", 4.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter1.getLogicalMeterId().toString(),
        logicalMeter2.getLogicalMeterId().toString()
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
  public void consumptionSeries() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");

    var logicalMeter1 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter1 = newPhysicalMeterEntity(logicalMeter1.getLogicalMeterId());
    newMeasurement(meter1, date, "Energy", 1.0);
    newMeasurement(meter1, date.plusHours(1), "Energy", 12.0);

    var logicalMeter2 = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter2 = newPhysicalMeterEntity(logicalMeter2.getLogicalMeterId());
    newMeasurement(meter2, date, "Energy", 3.0);
    newMeasurement(meter2, date.plusHours(1), "Energy", 8.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.ENERGY.name + ":kWh"
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter1.getLogicalMeterId().toString(),
        logicalMeter2.getLogicalMeterId().toString()
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
  public void oneMeterOneHour_ShoulOnlyInclude_ValueAtIntervalStart() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date.plusSeconds(1), "Power", 2.0);
    newMeasurement(meter, date.plusSeconds(2), "Power", 4.0);
    newMeasurement(meter, date.plusSeconds(3), "Power", 6.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
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
    newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:00:00.000Z"
        + "&quantity={quantiy}"
        + "&logicalMeterId={meterId}"
        + "&resolution=hour",
      MeasurementSeriesDto.class,
      Quantity.POWER.name,
      logicalMeter.getLogicalMeterId().toString()
    );

    ResponseEntity<List<MeasurementSeriesDto>> responseForNonZuluRequest =
      asUser().getList(
        "/measurements/average"
          + "?after={after}"
          + "&before={before}"
          + "&quantity={quantiy}"
          + "&logicalMeterId={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        "2018-03-06T03:00:00.000-02:00",
        "2018-03-06T07:00:00.000+01:00",
        Quantity.POWER.name,
        logicalMeter.getLogicalMeterId().toString()
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
  public void noPhysicalMeters_Returns404() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeterWithoutPhysical = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);

    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeterWithoutPhysical.getLogicalMeterId().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message).isEqualTo(
      String.format(
        "No physical meters connected to logical meter '%s' (%s)",
        logicalMeterWithoutPhysical.getLogicalMeterId(),
        logicalMeterWithoutPhysical.externalId
      )
    );
  }

  @Test
  public void noMatchingMeasurements_ReturnsEmptyList() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + Quantity.POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
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
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Power", 40000.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + Quantity.POWER.name + ":kW"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
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
  public void dayResolution_IncludesFromAndToDates_ButOnlyValuesAtResolution() {
    var date = ZonedDateTime.parse("2018-03-06T00:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date.plusSeconds(1), "Power", 1.0);
    newMeasurement(meter, date.plusDays(1).plusSeconds(2), "Power", 2.0);
    newMeasurement(meter, date.plusDays(1).plusSeconds(3), "Power", 4.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.minusDays(1)
          + "&before=" + date.plusDays(2).minusNanos(1)
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=day",
        logicalMeter.getLogicalMeterId().toString()
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
  public void monthResolution() {
    var date = ZonedDateTime.parse("2018-01-01T00:00:00Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Power", 1.0);
    newMeasurement(meter, date.plusMonths(1), "Power", 2.0);
    newMeasurement(meter, date.plusMonths(2), "Power", 4.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.plusHours(5)
          + "&before=" + date.plusMonths(2)
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=month",
        logicalMeter.getLogicalMeterId().toString()
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
  public void invalidParameterValuesReturnsHttp400_after() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=thisIsNotAValidTimestamp"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=month",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'after' timestamp: 'thisIsNotAValidTimestamp'.");
  }

  @Test
  public void invalidParameterValuesReturnsHttp400_before() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=thisIsNotAValidTimestamp"
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=month",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'before' timestamp: 'thisIsNotAValidTimestamp'.");
  }

  @Test
  public void invalidParameterValuesReturnsHttp400_resolution() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=NotAValidResolution",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'resolution' parameter: 'NotAValidResolution'.");
  }

  @Test
  public void invalidParameterValuesReturnsHttp400_logicalMeterId() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=month",
        "NotAValidUUID"
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Invalid UUID string: NotAValidUUID");
  }

  @Test
  public void invalidParameterValues_ReturnsEmptyList_quantity() {
    var date = ZonedDateTime.parse("2018-03-06T05:00:00.000Z");
    var logicalMeter = newLogicalMeterEntity(MeterDefinition.DISTRICT_HEATING_METER);
    newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());

    var response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=SomeUnknownQuantity"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.getLogicalMeterId().toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void missingParametersReturnsHttp400_after() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?to=2018-03-07T12:32:05.999Z"
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Missing 'after' parameter.");
  }

  @Test
  public void missingParametersReturnsHttp400_quantities() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'quantity' parameter.");
  }

  @Test
  public void toTimestampDefaultsToNow() {
    ZonedDateTime date = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0);
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Power", 1.0);

    List<MeasurementSeriesDto> response = asUser()
      .getList(
        "/measurements/average"
          + "?after={now}"
          + "&quantity={powerQuantity}:W"
          + "&logicalMeterId={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        date,
        Quantity.POWER.name,
        logicalMeter.getId().id
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
    var meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, after.plusHours(2), "Power", 1.0);

    MeasurementSeriesDto response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + after
          + "&before=" + before
          + "&quantity=" + Quantity.POWER.name + ":W"
          + "&logicalMeterId=%s",
        logicalMeter.getId().id
      ), MeasurementSeriesDto.class).getBody().get(0);
    assertThat(response.values).hasSize(23);
  }

  @Test
  public void findsAverageConsumptionForGasMeters_ByMeterIds() {
    var date = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    var logicalMeter = newLogicalMeterEntity(GAS_METER);
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Volume", 1.0);
    newMeasurement(meter, date.plusHours(1), "Volume", 2.0);
    newMeasurement(meter, date.plusHours(2), "Volume", 5.0);

    var response = asUser()
      .getList(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(3)
          + "&quantity=" + Quantity.VOLUME.name
          + "&logicalMeterId=" + logicalMeter.getId().id,
        MeasurementSeriesDto.class
      ).getBody();

    ZonedDateTime periodStartHour = date.truncatedTo(ChronoUnit.HOURS);

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(periodStartHour.toInstant(), 1.0),
        new MeasurementValueDto(periodStartHour.plusHours(1).toInstant(), 3.0),
        new MeasurementValueDto(periodStartHour.plusHours(2).toInstant(), null),
        new MeasurementValueDto(periodStartHour.plusHours(3).toInstant(), null)
      );
  }

  @Test
  public void findsAverage_ByCity() {
    var date = ZonedDateTime.parse("2018-02-01T01:00:00Z");

    var kungsbacka = kungsbacka().build();
    var kungsbackaLogical = newLogicalMeterEntity(kungsbacka);
    newMeasurement(
      newPhysicalMeterEntity(kungsbackaLogical.getLogicalMeterId()),
      date,
      Quantity.EXTERNAL_TEMPERATURE.name,
      1.0
    );

    var stockholmLogical = newLogicalMeterEntity(stockholm().build());
    newMeasurement(
      newPhysicalMeterEntity(stockholmLogical.getLogicalMeterId()),
      date,
      Quantity.EXTERNAL_TEMPERATURE.name,
      2.0
    );

    var response = asUser()
      .getList(
        Url.builder()
          .path("/measurements/average")
          .period(date, date)
          .quantity(Quantity.EXTERNAL_TEMPERATURE)
          .city(kungsbacka)
          .build(),
        MeasurementSeriesDto.class
      ).getBody();

    ZonedDateTime periodStartHour = date.truncatedTo(ChronoUnit.HOURS);

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(new MeasurementValueDto(periodStartHour.toInstant(), 1.0));
  }

  @Test
  public void findsAverageConsumptionForGasMeters_ByMedium() {
    var date = ZonedDateTime.parse("2018-02-01T01:00:00Z");
    var logicalMeter = newLogicalMeterEntity(GAS_METER);
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.getLogicalMeterId());
    newMeasurement(meter, date, "Volume", 1.0);
    newMeasurement(meter, date.plusHours(1), "Volume", 2.0);
    newMeasurement(meter, date.plusHours(2), "Volume", 5.0);

    var response = asUser()
      .getList(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(3)
          + "&quantity=" + Quantity.VOLUME.name
          + "&medium=Gas",
        MeasurementSeriesDto.class
      ).getBody();

    ZonedDateTime periodStartHour = date.truncatedTo(ChronoUnit.HOURS);

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(periodStartHour.toInstant(), 1.0),
        new MeasurementValueDto(periodStartHour.plusHours(1).toInstant(), 3.0),
        new MeasurementValueDto(periodStartHour.plusHours(2).toInstant(), null),
        new MeasurementValueDto(periodStartHour.plusHours(3).toInstant(), null)
      );
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
      organisationJpaRepository.delete(otherOrganisation);
    }
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return meterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created,
    String quantity,
    double value
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      created,
      quantityEntityMapper.toEntity(quantityProvider.getByName(quantity)),
      value,
      meter
    ));
  }

  private LogicalMeterEntity newLogicalMeterEntity(MeterDefinition meterDefinition) {
    UUID id = randomUUID();
    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);
    LogicalMeterEntity meter = new LogicalMeterEntity(
      new EntityPk(id, context().organisationEntity.id),
      id.toString(),
      ZonedDateTime.now(),
      meterDefinitionEntity,
      DEFAULT_UTC_OFFSET
    );

    return logicalMeterJpaRepository.save(meter);
  }

  private LogicalMeterEntity newLogicalMeterEntity(Location location) {
    var meter = newLogicalMeterEntity(MeterDefinition.ROOM_SENSOR_METER);
    var primaryKey = new Pk(meter.getLogicalMeterId(), meter.getOrganisationId());
    meter.location = LocationEntityMapper.toEntity(primaryKey, location);
    return logicalMeterJpaRepository.save(meter);
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
}
