package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
    given(series(logicalMeter, POWER, 1.0, 2.0));

    var uninterestingMeter = given(logicalMeter());
    given(series(uninterestingMeter, POWER, 99.0, 100.0));

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
      singletonList(new MeasurementSeriesDto(
        SERIES_ID_AVERAGE_POWER,
        POWER.name,
        POWER.storageUnit,
        AVERAGE,
        asList(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 2.0)
        )
      )));
  }

  @Test
  public void oneMeter_TwoHours() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());
    given(series(logicalMeter, POWER, 1.0, 2.0));

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
      singletonList(new MeasurementSeriesDto(
        SERIES_ID_AVERAGE_POWER,
        POWER.name,
        POWER.storageUnit,
        AVERAGE,
        asList(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 2.0)
        )
      )));
  }

  @Test
  public void twoMeters_TwoHours() {
    var date = context().now();
    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(series(logicalMeter1, POWER, 1.0, 2.0));
    given(series(logicalMeter2, POWER, 3.0, 4.0));

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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name,
          POWER.storageUnit,
          AVERAGE,
          asList(
            new MeasurementValueDto(date.toInstant(), 2.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), 3.0)
          )
        )));
  }

  @Test
  public void consumptionSeries() {
    var date = context().now();

    var logicalMeter1 = given(logicalMeter());
    var logicalMeter2 = given(logicalMeter());

    given(series(logicalMeter1, ENERGY, 1.0, 12.0));
    given(series(logicalMeter2, ENERGY, 3.0, 8.0));

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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_ENERGY,
          Quantity.ENERGY.name,
          "kWh",
          AVERAGE,
          asList(
            new MeasurementValueDto(date.toInstant(), 8.0),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          )
        )));
  }

  @Test
  public void oneMeterOneHour_ShoulOnlyInclude_ValueAtIntervalStart() {
    var date = context().now();

    var logicalMeter = given(logicalMeter());
    given(series(logicalMeter, POWER, Duration.ofSeconds(1), 2.0, 4.0, 6.0));

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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name,
          POWER.storageUnit,
          AVERAGE,
          singletonList(new MeasurementValueDto(date.toInstant(), 2.0))
        )));
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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name,
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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name, "W",
          AVERAGE,
          singletonList(new MeasurementValueDto(date.toInstant(), null))
        )));
  }

  @Test
  public void allowsOverridingDefinitionsPresentationUnit() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());
    given(series(logicalMeter, POWER, 40000.0));

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

    given(series(logicalMeter, POWER, Duration.ofSeconds(1), 1.0, 2.0));

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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name,
          "W",
          AVERAGE,
          asList(
            new MeasurementValueDto(date.minusDays(1).toInstant(), null),
            new MeasurementValueDto(date.toInstant(), 1.0),
            new MeasurementValueDto(date.plusDays(1).toInstant(), null)
          )
        )
      ));
  }

  @Test
  @Ignore("Is this reasonable? Should we ignore the after parameter?")
  public void monthResolution() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());

    given(series(
      logicalMeter,
      POWER,
      date,
      Period.ofMonths(1),
      1.0, 2.0, 4.0
    ));

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
      singletonList(
        new MeasurementSeriesDto(
          SERIES_ID_AVERAGE_POWER,
          POWER.name,
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
    given(series(logicalMeter, POWER, date, Duration.ofHours(1), 1.0));

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
    given(series(logicalMeter, VOLUME, 1.0, 2.0, 5.0));

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
    given(series(kungsbackaLogical, EXTERNAL_TEMPERATURE, 1.0));

    var stockholmLogical = given(logicalMeter().meterDefinition(DEFAULT_ROOM_SENSOR)
      .location(stockholm().build()));
    given(series(stockholmLogical, EXTERNAL_TEMPERATURE, 2.0));

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

    given(series(logicalMeter, VOLUME, 1.0, 2.0, 5.0));

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
