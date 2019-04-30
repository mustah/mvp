package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_ROOM_SENSOR;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.EXTERNAL_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.POWER;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class MeasurementControllerAverageTest extends IntegrationTest {

  private static final String SERIES_ID_AVERAGE_POWER = "average-Power";
  private static final String SERIES_ID_AVERAGE_ENERGY = "average-Energy";
  private static final String AVERAGE = "average";

  @Test
  public void oneMeter_OnlyIncludesAskedForMeters() {
    var date = context().now();

    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));

    var uninterestingMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(uninterestingMeter)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(99.0, 100.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(MeasurementSeriesDto.builder()
        .id(SERIES_ID_AVERAGE_POWER)
        .quantity(POWER.name)
        .unit(POWER.storageUnit)
        .label(AVERAGE)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 2.0)
        ))
        .build()));
  }

  @Test
  public void oneMeter_TwoHours() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(MeasurementSeriesDto.builder()
        .id(SERIES_ID_AVERAGE_POWER)
        .quantity(POWER.name)
        .unit(POWER.storageUnit)
        .label(AVERAGE)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 2.0)
        ))
        .build()));
  }

  @Test
  public void twoMeters_TwoHours() {
    var date = context().now();
    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(measurementSeries()
      .forMeter(logicalMeter1)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(logicalMeter2)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(3.0, 4.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter1.id.toString(),
        logicalMeter2.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit(POWER.storageUnit)
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), 2.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), 3.0)
          ))
          .build()));
  }

  @Test
  public void twoMeters_TwoHours_ResolutionAll() {
    var date = context().now();
    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(measurementSeries()
      .forMeter(logicalMeter1)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(5))
      .withValues(1.0, 2.0, 3.0));
    given(measurementSeries()
      .forMeter(logicalMeter2)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(10))
      .withValues(10.0, 11.0, 12.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusMinutes(1)
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=all",
        logicalMeter1.id.toString(),
        logicalMeter2.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit(POWER.storageUnit)
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), 5.5),
            new MeasurementValueDto(date.plusSeconds(5).toInstant(), 2.0),
            new MeasurementValueDto(date.plusSeconds(10).toInstant(), 7.0),
            new MeasurementValueDto(date.plusSeconds(20).toInstant(), 12.0)
          ))
          .build()));
  }

  @Test
  public void twoMeters_TwoHours_ResolutionAll_Consumption() {
    var date = context().now();
    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(measurementSeries()
      .forMeter(logicalMeter1)
      .withQuantity(ENERGY)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(5))
      .withValues(1.0, 2.0, 4.0, 8.0));
    given(measurementSeries()
      .forMeter(logicalMeter2)
      .withQuantity(ENERGY)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(10))
      .withValues(10.0, 15.0, 27.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusMinutes(1)
          + "&quantity=" + ENERGY.name
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=all",
        logicalMeter1.id.toString(),
        logicalMeter2.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_ENERGY)
          .quantity(ENERGY.name)
          .unit(ENERGY.storageUnit)
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), 3.0),
            new MeasurementValueDto(date.plusSeconds(5).toInstant(), 2.0),
            new MeasurementValueDto(date.plusSeconds(10).toInstant(), 8.0),
            new MeasurementValueDto(date.plusSeconds(15).toInstant(), null),
            new MeasurementValueDto(date.plusSeconds(20).toInstant(), null)
          ))
          .build()));
  }

  @Test
  public void consumptionSeries() {
    var date = context().now();

    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(measurementSeries()
      .forMeter(logicalMeter1)
      .withQuantity(ENERGY)
      .startingAt(context().now())
      .withValues(1.0, 12.0));
    given(measurementSeries()
      .forMeter(logicalMeter2)
      .withQuantity(ENERGY)
      .startingAt(context().now())
      .withValues(3.0, 8.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + Quantity.ENERGY.name + ":kWh"
          + "&logicalMeterId=%s"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter1.id.toString(),
        logicalMeter2.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_ENERGY)
          .quantity(Quantity.ENERGY.name)
          .unit("kWh")
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), 8.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          ))
          .build()));
  }

  @Test
  public void oneMeterOneHour_ShoulOnlyInclude_ValueAtIntervalStart() {
    var date = context().now();

    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .withInterval(Duration.ofSeconds(1))
      .startingAt(context().now())
      .withValues(2.0, 4.0, 6.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit(POWER.storageUnit)
          .label(AVERAGE)
          .values(List.of(new MeasurementValueDto(date.toInstant(), 2.0)))
          .build()));
  }

  @Test
  public void timeZoneInformationIsConsidered() {
    var logicalMeter = given(logicalMeter());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      "/measurements/average"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:00:00.000Z"
        + "&quantity={quantiy}"
        + "&logicalMeterId={meterId}"
        + "&resolution=hour",
      MeasurementSeriesDto.class,
      POWER.name,
      logicalMeter.id.toString()
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
        POWER.name,
        logicalMeter.id.toString()
      );

    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit("W")
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(
              ZonedDateTime.parse("2018-03-06T05:00:00.000Z").toInstant(),
              null
            ),
            new MeasurementValueDto(
              ZonedDateTime.parse("2018-03-06T06:00:00.000Z").toInstant(),
              null
            )
          ))
          .build()
      ));
    assertThat(response.getBody()).isEqualTo(responseForNonZuluRequest.getBody());
  }

  @Test
  public void noActivePhysicalMeters_ReturnsEmpty() {
    var date = context().now();
    var logicalMeterWithoutPhysical = given(physicalMeter().activePeriod(PeriodRange.empty()));

    var response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeterWithoutPhysical.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void noMatchingMeasurements_ReturnsListOfNullAverages() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + POWER.name
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit("W")
          .label(AVERAGE)
          .values(List.of(new MeasurementValueDto(date.toInstant(), null)))
          .build()));
  }

  @Test
  public void allowsOverridingDefinitionsPresentationUnit() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(context().now())
      .withValues(40000.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date
          + "&quantity=" + POWER.name + ":kW"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
      .extracting("quantity")
      .containsExactly(POWER.name);

    assertThat(response.getBody())
      .extracting(m -> m.unit, m -> m.quantity, m -> m.values)
      .containsExactly(tuple(
        "kW",
        POWER.name,
        List.of(new MeasurementValueDto(date.toInstant(), 40.0))
      ));
  }

  @Test
  public void dayResolution_IncludesFromAndToDates_ButOnlyValuesAtResolution() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());

    given(measurementSeries().forMeter(logicalMeter)
      .withQuantity(POWER)
      .withInterval((TemporalAmount) Duration.ofSeconds(1))
      .startingAt(context().now())
      .withValues(1.0, 2.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.minusDays(1)
          + "&before=" + date.plusDays(2).minusNanos(1)
          + "&quantity=" + POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=day",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit("W")
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.minusDays(1).toInstant(), null),
            new MeasurementValueDto(date.toInstant(), 1.0),
            new MeasurementValueDto(date.plusDays(1).toInstant(), null)
          ))
          .build()
      ));
  }

  @Test
  @Ignore("Is this reasonable? Should we ignore the after parameter?")
  public void monthResolution() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(Period.ofMonths(1))
      .withValues(1.0, 2.0, 4.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date.plusHours(5)
          + "&before=" + date.plusMonths(2)
          + "&quantity=" + POWER.name + ":W"
          + "&logicalMeterId=%s"
          + "&resolution=month",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      List.of(
        MeasurementSeriesDto.builder()
          .id(SERIES_ID_AVERAGE_POWER)
          .quantity(POWER.name)
          .unit("W")
          .label(AVERAGE)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), 1.0),
            new MeasurementValueDto(date.plusMonths(1).toInstant(), 2.0),
            new MeasurementValueDto(date.plusMonths(2).toInstant(), 4.0)
          ))
          .build()
      ));
  }

  @Test
  public void invalidParameterValuesReturnsHttp400_after() {
    var response = asUser()
      .get(String.format(
        "/measurements/average"
          + "?after=thisIsNotAValidTimestamp"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantity=" + POWER.name + ":W"
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
          + "&quantity=" + POWER.name + ":W"
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
          + "&quantity=" + POWER.name + ":W"
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
          + "&quantity=" + POWER.name + ":W"
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
    var logicalMeter = given(logicalMeter());

    var response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(1)
          + "&quantity=SomeUnknownQuantity"
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
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
          + "&quantity=" + POWER.name + ":W"
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
    ZonedDateTime date = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS);
    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(Duration.ofHours(1))
      .withValues(1.0));

    List<MeasurementSeriesDto> response = asUser()
      .getList(
        "/measurements/average"
          + "?after={now}"
          + "&quantity={powerQuantity}:W"
          + "&logicalMeterId={meterId}"
          + "&resolution=hour",
        MeasurementSeriesDto.class,
        date,
        POWER.name,
        logicalMeter.id
      ).getBody();

    assertThat(response)
      .flatExtracting("values")
      .containsExactly(
        new MeasurementValueDto(date.toInstant(), 1.0)
      );
  }

  @Test
  public void resolutionDefaultsToHourForPeriodLessThanADay() {
    var after = context().now().plusHours(1);
    var before = after.plusDays(1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1);
    var logicalMeter = given(logicalMeter());

    MeasurementSeriesDto response = asUser()
      .getList(String.format(
        "/measurements/average"
          + "?after=" + after
          + "&before=" + before
          + "&quantity=" + POWER.name + ":W"
          + "&logicalMeterId=%s",
        logicalMeter.id
      ), MeasurementSeriesDto.class).getBody().get(0);
    assertThat(response.values).hasSize(23);
  }

  @Test
  public void findsAverageConsumptionForGasMeters_ByMeterIds() {
    var date = context().now();
    var logicalMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(VOLUME)
      .startingAt(context().now())
      .withValues(1.0, 2.0, 5.0));

    var response = asUser()
      .getList(
        "/measurements/average"
          + "?after=" + date
          + "&before=" + date.plusHours(3)
          + "&quantity=" + Quantity.VOLUME.name
          + "&logicalMeterId=" + logicalMeter.id,
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
    var date = context().now();

    var kungsbacka = kungsbacka().build();
    var kungsbackaLogical = given(logicalMeter().meterDefinition(DEFAULT_ROOM_SENSOR)
      .location(kungsbacka));
    given(measurementSeries()
      .forMeter(kungsbackaLogical)
      .withQuantity(EXTERNAL_TEMPERATURE)
      .startingAt(context().now())
      .withValues(1.0));

    var stockholmLogical = given(logicalMeter().meterDefinition(DEFAULT_ROOM_SENSOR)
      .location(stockholm().build()));
    given(measurementSeries()
      .forMeter(stockholmLogical)
      .withQuantity(EXTERNAL_TEMPERATURE)
      .startingAt(context().now())
      .withValues(2.0));

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
    var date = context().now();
    var logicalMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(VOLUME)
      .startingAt(context().now())
      .withValues(1.0, 2.0, 5.0));

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
    measurementJpaRepository.deleteAll();
  }
}
