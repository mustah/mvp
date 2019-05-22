package com.elvaco.mvp.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.DIFFERENCE_TEMPERATURE;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Test all selection parameters that can be used to filter measurement average.
 */
public class MeasurementControllerSelectionAverageTest extends IntegrationTest {

  public static final String DIFFTEMP_NAME = "Difference temperature";
  private static final String SERIES_ID_AVERAGE = "average-Difference temperature";
  private static final double DIFFTEMP_VALUE1 = 47.0;
  private static final double DIFFTEMP_VALUE2 = 18.0;

  @Test
  public void average_byFacility() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter());
    LogicalMeter meter2 = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&facility=" + meter2.externalId
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byOrganisation() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter());
    LogicalMeter meter2 = given(logicalMeter().organisationId(given(organisation()).id));
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&organisation=" + meter2.organisationId
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byLocationCity() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter().location(stockholm().city("Kungsbacka").build()));
    LogicalMeter meter2 = given(logicalMeter().location(kungsbacka().city("Granbergsbyn").build()));
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&city=" + meter2.location.getCountry() + "," + meter2.location.getCity()
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byLocationAddress() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter().location(kungsbacka().address("Storg 1").build()));
    LogicalMeter meter2 = given(logicalMeter().location(kungsbacka().address("Storg 2").build()));
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    String addressParam = meter2.location.getCountry()
      + "," + meter2.location.getCity()
      + "," + urlEncode(meter2.location.getAddress());

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&address=" + addressParam
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byMedium() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_COOLING));
    LogicalMeter meter2 = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&medium=" + urlEncode(Medium.DISTRICT_HEATING)
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byReported() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter());
    LogicalMeter meter2 = given(logicalMeter());
    given(statusLog(meter2).status(StatusType.ERROR));

    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&reported=" + StatusType.ERROR
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byAlarm() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter());
    LogicalMeter meter2 = given(logicalMeter());
    given(alarm(meter2).mask(32).description("Alarm"));

    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&alarm=" + "yes"
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_bySecondaryAddress() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter(), physicalMeter());
    LogicalMeter meter2 = given(logicalMeter(), physicalMeter());
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&secondaryAddress=" + meter2.physicalMeters.get(0).address
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  @Test
  public void average_byGatewaySerial() {
    ZonedDateTime date = context().now();

    LogicalMeter meter1 = given(logicalMeter().gateway(gateway().build()));
    LogicalMeter meter2 = given(logicalMeter().gateway(gateway().build()));
    given(measurementSeries()
      .forMeter(meter1)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE1));
    given(measurementSeries()
      .forMeter(meter2)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFFTEMP_VALUE2));

    List<MeasurementSeriesDto> measurements = asSuperAdmin()
      .getList(
        "/measurements/average?resolution=hour"
          + "&gatewaySerial=" + meter2.gateways.get(0).serial
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(measurements)
      .extracting(m -> m.id, m -> m.quantity, m -> m.values)
      .containsExactly(
        tuple(
          SERIES_ID_AVERAGE,
          DIFFTEMP_NAME,
          List.of(new MeasurementValueDto(date.toInstant(), DIFFTEMP_VALUE2))
        ));
  }

  private String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
