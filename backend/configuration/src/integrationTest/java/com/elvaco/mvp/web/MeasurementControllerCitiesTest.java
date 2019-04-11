package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class MeasurementControllerCitiesTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void oneCityAverage() {
    ZonedDateTime start = context().now();
    LogicalMeter storaGatan1 = given(
      logicalMeter().location(stockholm().address("stora gatan 1").build())
    );

    LogicalMeter storaGatan2 = given(
      logicalMeter().location(stockholm().address("stora gatan 2").build())
    );

    given(measurementSeries()
      .forMeter(storaGatan1)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(storaGatan2)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(3.0, 4.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .parameter("label", "Stockholm")
          .city("sverige,stockholm")
          .resolution(TemporalResolution.hour)
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      MeasurementSeriesDto.builder()
        .id("average-Power")
        .quantity(Quantity.POWER.name)
        .unit("W")
        .label("Stockholm")
        .medium(null)
        .values(asList(
          new MeasurementValueDto(start.toInstant(), 2.0),
          new MeasurementValueDto(start.plusHours(1).toInstant(), 3.0)
        ))
        .build()
    );
  }

  @Test
  public void cityAverageOnlyIncludesRequestedCity() {
    var start = context().now();

    LogicalMeter stockholm1 = given(
      logicalMeter().location(stockholm().address("stora gatan 1").build()
      ));

    LogicalMeter stockholm2 = given(
      logicalMeter().location(stockholm().address("stora gatan 2").build()
      ));

    LogicalMeter kungsbacka = given(
      logicalMeter().location(kungsbacka().build()
      ));

    given(measurementSeries()
      .forMeter(stockholm1)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(stockholm2)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(3.0, 4.0));
    given(measurementSeries()
      .forMeter(kungsbacka)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(10.0, 10.0));

    var response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .city("sverige,stockholm")
          .resolution(TemporalResolution.hour)
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var measurementSeries = response.getBody();

    assertSoftly(softly -> {
      softly.assertThat(measurementSeries)
        .flatExtracting("values")
        .extracting("value")
        .containsExactly(2.0, 3.0);
    });
  }

  @Test
  public void cityAverageCanContainBothMetersWithRequestedQuantitiesAndOtherMeters() {
    Location kiruna = new LocationBuilder()
      .country("sverige")
      .city("kiruna")
      .address("stora gatan 1")
      .build();

    LogicalMeter roomSensorMeter = given(logicalMeter().location(kiruna)
      .meterDefinition(MeterDefinition.DEFAULT_ROOM_SENSOR));

    LogicalMeter gasMeter = given(logicalMeter().location(kiruna)
      .meterDefinition(MeterDefinition.DEFAULT_GAS));

    ZonedDateTime start = context().now();

    given(measurementSeries()
      .forMeter(roomSensorMeter)
      .withQuantity(Quantity.EXTERNAL_TEMPERATURE)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(gasMeter)
      .withQuantity(Quantity.VOLUME)
      .startingAt(context().now())
      .withValues(10.0, 11.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", "External temperature")
          .city("sverige,kiruna")
          .resolution(TemporalResolution.hour)
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      MeasurementSeriesDto.builder()
        .id("average-External temperature")
        .quantity(Quantity.EXTERNAL_TEMPERATURE.name)
        .unit(Quantity.EXTERNAL_TEMPERATURE.storageUnit)
        .label("average")
        .medium(null)
        .values(asList(
          new MeasurementValueDto(start.toInstant(), 1.0),
          new MeasurementValueDto(start.plusHours(1).toInstant(), 2.0)
        ))
        .build()
    );
  }

  @Test
  public void cityAverageIsEmptyWhenCityOnlyContainNonMatchingQuantities() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter().location(stockholm().build()));
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", "Relative humidity")
          .city("sverige,stockholm")
          .resolution(TemporalResolution.hour)
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void cityAverageIsEmptyWhenNoMetersExistsInCity() {
    ZonedDateTime start = context().now();

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.VOLUME.name)
          .city("sverige,stockholm")
          .resolution(TemporalResolution.hour).build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void twoCitiesArePartOfSingleAverage() {
    ZonedDateTime start = context().now();

    var stockholm = given(logicalMeter().location(stockholm().build()));
    var kungsbacka = given(logicalMeter().location(kungsbacka().build()));

    given(measurementSeries()
      .forMeter(stockholm)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(kungsbacka)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(10.0, 10.0));

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsAverageUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .city("sverige,stockholm")
          .city("sverige,kungsbacka")
          .resolution(TemporalResolution.hour)
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      MeasurementSeriesDto.builder()
        .id("average-Power")
        .quantity(Quantity.POWER.name)
        .unit("W")
        .label("average")
        .medium(null)
        .values(asList(
          new MeasurementValueDto(start.toInstant(), 5.5),
          new MeasurementValueDto(start.plusHours(1).toInstant(), 6.0)
        ))
        .build()
    );
  }

  @Test
  public void averageForCityIsSameAsAverageForAllMetersInCity() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter().location(stockholm().address("1").build()));
    var meterTwo = given(logicalMeter().location(stockholm().address("2").build()));

    given(measurementSeries()
      .forMeter(meterOne)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.0, 2.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(3.0, 4.0));

    Url cityAverageUrl = measurementsAverageUrl()
      .period(start, start.plusHours(1))
      .city("sverige,stockholm")
      .resolution(TemporalResolution.hour)
      .parameter("quantity", Quantity.POWER.name + ":W")
      .build();

    Url metersAverageUrl = Url
      .builder()
      .path("/measurements/average")
      .period(start, start.plusHours(1))
      .resolution(TemporalResolution.hour)
      .parameter("quantity", Quantity.POWER.name + ":W")
      .parameter("id", List.of(meterOne.id, meterTwo.id))
      .build();

    ResponseEntity<List<MeasurementSeriesDto>> cityAverageResponse = asUser()
      .getList(cityAverageUrl, MeasurementSeriesDto.class
      );

    ResponseEntity<List<MeasurementSeriesDto>> metersAverageResponse = asUser()
      .getList(
        metersAverageUrl,
        MeasurementSeriesDto.class
      );

    assertThat(cityAverageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(metersAverageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<MeasurementValueDto> expected = List.of(
      new MeasurementValueDto(start.toInstant(), 2.0),
      new MeasurementValueDto(start.plusHours(1).toInstant(), 3.0)
    );

    assertThat(cityAverageResponse.getBody()).extracting("values").containsExactly(expected);
    assertThat(metersAverageResponse.getBody()).extracting("values").containsExactly(expected);
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    measurementJpaRepository.deleteAll();
  }

  private Url.UrlBuilder measurementsAverageUrl() {
    return Url.builder().path("/measurements/average");
  }
}
