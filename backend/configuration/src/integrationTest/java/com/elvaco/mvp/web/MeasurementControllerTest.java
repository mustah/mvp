package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;
import static com.elvaco.mvp.core.domainmodels.DisplayMode.READOUT;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.domainmodels.Quantity.DIFFERENCE_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.EXTERNAL_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.HUMIDITY;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.core.domainmodels.Units.DEGREES_CELSIUS;
import static com.elvaco.mvp.core.domainmodels.Units.KELVIN;
import static com.elvaco.mvp.core.domainmodels.Units.KILOWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.MEGAWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.PERCENT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

public class MeasurementControllerTest extends IntegrationTest {

  private static final Offset<Double> OFFSET = within(0.000_000_000_000_1);

  private static final double ENERGY_VALUE = 9999.0;
  private static final double DIFF_TEMP_VALUE_CELSIUS = 285.59;
  private static final double DIFF_TEMP_VALUE_KELVIN = 558.74;
  private static final double HUMIDITY_VALUE = 55.3;
  private static final double TEMP_VALUE = 21.7;

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    ZonedDateTime date = context().now();

    LogicalMeter meter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(series(meter, DIFFERENCE_TEMPERATURE, date, DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?resolution=hour"
          + "&logicalMeterId=" + meter.id
          + "&quantity=Difference+temperature"
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
    ZonedDateTime date = context().now();

    LogicalMeter meter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(diffTempMeasurement(meter, date));

    List<MeasurementSeriesDto> measurements = asUser()
      .getList(
        "/measurements?quantity=Difference+temperature:K"
          + "&logicalMeterId=" + meter.id
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1),
        MeasurementSeriesDto.class
      )
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Difference temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).values.get(0).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void canNotSeeMeasurementsFromMeterBelongingToOtherOrganisation() {
    ZonedDateTime date = context().now();
    UUID organisationId = given(organisation()).getId();
    var meter = given(logicalMeter().organisationId(organisationId));
    given(series(meter, DIFFERENCE_TEMPERATURE, date, DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?logicalMeterId=" + meter.id
          + "&after=" + date
          + "&before=" + date,
        MeasurementDto.class
      )
      .getBody();

    assertThat(measurements).isEmpty();
  }

  @Test
  public void superAdminCanSeeAllMeasurements() {
    ZonedDateTime date = context().now();

    UUID organisationId = given(organisation()).getId();
    var firstOrganisationsMeter =
      given(logicalMeter().organisationId(organisationId));
    given(series(firstOrganisationsMeter, DIFFERENCE_TEMPERATURE, date, DIFF_TEMP_VALUE_CELSIUS));

    var secondOrganisationsMeter = given(logicalMeter());
    given(series(secondOrganisationsMeter, DIFFERENCE_TEMPERATURE, date, DIFF_TEMP_VALUE_CELSIUS));

    assertThat(getListAsSuperAdmin(
      "/measurements?logicalMeterId=" + firstOrganisationsMeter.id
        + "&logicalMeterId=" + secondOrganisationsMeter.id
        + "&quantity=Difference+temperature"
        + "&resolution=hour"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
      )
    ).hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriod() {
    ZonedDateTime date = context().now();

    var heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(
      diffTempMeasurement(heatMeter, date.minusHours(1)),
      diffTempMeasurement(heatMeter, date.plusHours(1)),
      energyMeasurement(heatMeter, date.plusHours(1))
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "quantity=Difference+temperature"
        + "&logicalMeterId=" + heatMeter.id
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void fetchMeasurementsForMeterInPeriod() {
    ZonedDateTime date = context().now();

    LogicalMeter heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(
      diffTempMeasurement(heatMeter, date),
      diffTempMeasurement(heatMeter, date.plusHours(2))
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "logicalMeterId=" + heatMeter.id
        + "&quantity=Difference+temperature,Energy"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents)
      .contains(
        new MeasurementSeriesDto(
          heatMeter.id.toString(),
          "Difference temperature",
          "K",
          getExpecedLabel(heatMeter),
          MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name,
          asList(
            new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          )
        )
      );
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriodWithNonDefaultUnit() {
    ZonedDateTime date = context().now();

    LogicalMeter heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(
      diffTempMeasurement(heatMeter, date.minusHours(1)),
      diffTempMeasurement(heatMeter, date.plusHours(1))
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantity=Difference+temperature:K"
        + "&after=" + date
        + "&before=" + date.plusHours(1)
        + "&logicalMeterId=" + heatMeter.id.toString()
        + "&resolution=hour");

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  @Test
  public void fetchMeasurementsWithoutQuantityGivesAllQuantities() {
    ZonedDateTime date = context().now();

    LogicalMeter roomSensorMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_ROOM_SENSOR));
    given(
      tempMeasurement(roomSensorMeter, date),
      humidityMeasurement(roomSensorMeter, date)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "after=" + date
        + "&before=" + date
        + "&logicalMeterId=" + roomSensorMeter.id.toString()
        + "&resolution=hour");

    assertThat(contents).hasSize(2);

    assertThat(contents).filteredOn(dto -> dto.quantity.equals(EXTERNAL_TEMPERATURE.name))
      .extracting(d -> d.unit, d -> d.values.size(), d -> d.values.get(0).value)
      .containsOnly(tuple(DEGREES_CELSIUS, 1, TEMP_VALUE));

    assertThat(contents).filteredOn(dto -> dto.quantity.equals(HUMIDITY.name))
      .extracting(d -> d.unit, d -> d.values.size(), d -> d.values.get(0).value)
      .containsOnly(tuple(PERCENT, 1, HUMIDITY_VALUE));
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    ZonedDateTime date = context().now();

    LogicalMeter heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(
      diffTempMeasurement(heatMeter, date),
      diffTempMeasurement(heatMeter, date.plusHours(1)),
      energyMeasurement(heatMeter, date)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature:K,Energy:kWh"
          + "&logicalMeterId=" + heatMeter.id
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        heatMeter.id.toString(),
        "Difference temperature",
        "K",
        getExpecedLabel(heatMeter),
        MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name,
        asList(
          new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
          new MeasurementValueDto(date.plusHours(1).toInstant(), DIFF_TEMP_VALUE_KELVIN)
        )
      ),
      new MeasurementSeriesDto(
        heatMeter.id.toString(),
        "Energy",
        "kWh",
        getExpecedLabel(heatMeter),
        MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name,
        asList(
          new MeasurementValueDto(date.toInstant(), null),
          new MeasurementValueDto(date.plusHours(1).toInstant(), null)
        )
      )
    );
  }

  @Test
  public void fetchMeasurementsUsesPreferredQuantityUnitsForAllMeters() {
    ZonedDateTime date = context().now();

    var meterDefinition1 = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .quantities(Set.of(
        new DisplayQuantity(DIFFERENCE_TEMPERATURE, READOUT, 1, DEGREES_CELSIUS),
        new DisplayQuantity(ENERGY, CONSUMPTION, 2, MEGAWATT_HOURS)
      )));

    var meterDefinition2 = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .quantities(Set.of(
        new DisplayQuantity(DIFFERENCE_TEMPERATURE, READOUT, 2, KELVIN),
        new DisplayQuantity(ENERGY, CONSUMPTION, 3, KILOWATT_HOURS)
      )));

    LogicalMeter meter11 = given(logicalMeter().meterDefinition(meterDefinition1));
    LogicalMeter meter12 = given(logicalMeter().meterDefinition(meterDefinition1));
    LogicalMeter meter2 = given(logicalMeter().meterDefinition(meterDefinition2));

    given(
      diffTempMeasurement(meter11, date),
      energyMeasurement(meter11, date)
    );

    given(
      diffTempMeasurement(meter12, date),
      energyMeasurement(meter12, date)
    );

    given(
      diffTempMeasurement(meter2, date),
      energyMeasurement(meter2, date)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature,Energy"
          + "&logicalMeterId=" + meter11.id
          + "&logicalMeterId=" + meter12.id
          + "&logicalMeterId=" + meter2.id
          + "&resolution=hour"
          + "&after=" + date
          + "&before=" + date.plusHours(1));

    assertThat(contents)
      .extracting(dto -> dto.id, dto -> dto.quantity, dto -> dto.unit)
      .containsExactlyInAnyOrder(
        tuple(meter11.id.toString(), DIFFERENCE_TEMPERATURE.name, DEGREES_CELSIUS),
        tuple(meter12.id.toString(), DIFFERENCE_TEMPERATURE.name, DEGREES_CELSIUS),
        tuple(meter2.id.toString(), DIFFERENCE_TEMPERATURE.name, DEGREES_CELSIUS),

        tuple(meter11.id.toString(), ENERGY.name, MEGAWATT_HOURS),
        tuple(meter12.id.toString(), ENERGY.name, MEGAWATT_HOURS),
        tuple(meter2.id.toString(), ENERGY.name, MEGAWATT_HOURS)
      );
  }

  @Test
  public void measurementSeriesAreLabeledWithMeterExternalId() {
    ZonedDateTime date = context().now();
    LogicalMeter logicalMeter = given(
      logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
    );

    given(diffTempMeasurement(logicalMeter, date));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Difference+temperature"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=hour"
        + "&after=" + date
        + "&before=" + date.plusHours(1));

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.label).isEqualTo(getExpecedLabel(logicalMeter));
  }

  @Test
  public void unknownUnitSuppliedForScaling() {
    ZonedDateTime date = context().now();
    LogicalMeter heatMeter = given(
      logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
    );

    given(diffTempMeasurement(heatMeter, date));

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:unknownUnit"
          + "&logicalMeterId=" + heatMeter.id.toString()
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
    ZonedDateTime date = context().now();
    LogicalMeter heatMeter = given(
      logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
    );

    given(diffTempMeasurement(heatMeter, date));

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:kWh"
          + "&logicalMeterId=" + heatMeter.id
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
    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:kWh",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Missing 'after' parameter.");
  }

  @Test
  public void consumptionSeriesIsDisplayedWithConsumptionValuesAtFirstTimeInInterval() {
    ZonedDateTime when = context().now();
    var consumptionMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(
      series(consumptionMeter, VOLUME, 25, 35, 55)
    );

    List<MeasurementSeriesDto> list = asUser()
      .getList(
        "/measurements?resolution=hour&quantity=Volume"
          + "&logicalMeterId=" + consumptionMeter.id
          + "&after=" + when
          + "&before=" + when.plusHours(2),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(list).containsExactly(
      new MeasurementSeriesDto(
        consumptionMeter.id.toString(),
        "Volume",
        "m³",
        getExpecedLabel(consumptionMeter),
        consumptionMeter.meterDefinition.medium.name,
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
    ZonedDateTime when = context().now();
    var consumptionMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(series(consumptionMeter, VOLUME, 25, 35, 55));

    List<MeasurementSeriesDto> seriesDto = asUser()
      .getList(String.format(
        "/measurements?resolution=hour&quantity=Volume&logicalMeterId=%s"
          + "&after=%s&before=%s",
        consumptionMeter.id,
        when,
        when.plusHours(1)
      ), MeasurementSeriesDto.class).getBody();

    assertThat(seriesDto).containsExactly(
      new MeasurementSeriesDto(
        consumptionMeter.id.toString(),
        "Volume",
        "m³",
        getExpecedLabel(consumptionMeter),
        consumptionMeter.meterDefinition.medium.name,
        asList(
          new MeasurementValueDto(when.plusHours(0).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 20.0)
        )
      )
    );
  }

  @Test
  public void findsConsumptionForGasMeters() {
    ZonedDateTime when = context().now();
    var logicalMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(
      series(logicalMeter, VOLUME, 1, 2, 5)
    );

    List<MeasurementSeriesDto> response = asUser()
      .getList(String.format(
        "/measurements"
          + "?after=" + when
          + "&before=" + when.plusHours(2)
          + "&quantity=" + Quantity.VOLUME.name
          + "&logicalMeterId=%s",
        logicalMeter.id
      ), MeasurementSeriesDto.class).getBody();

    ZonedDateTime periodStartHour = when.truncatedTo(ChronoUnit.HOURS);

    assertThat(response)
      .flatExtracting("values")
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
    ZonedDateTime after = context().now();
    ZonedDateTime before = context().now().plusDays(1);
    var logicalMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    List<MeasurementSeriesDto> response = asUser()
      .getList(
        "/measurements"
          + "?after=" + after
          + "&before=" + before
          + "&quantity=Floop"
          + "&logicalMeterId=" + logicalMeter.id, MeasurementSeriesDto.class).getBody();

    assertThat(response).hasSize(0);
  }

  private String getExpecedLabel(LogicalMeter meter) {
    assertThat(meter.physicalMeters.size()).isEqualTo(1);
    return meter.externalId + "-" + meter.physicalMeters.get(0).address;
  }

  private Measurement.MeasurementBuilder energyMeasurement(LogicalMeter meter, ZonedDateTime date) {
    return measurement(meter)
      .created(date)
      .unit("J")
      .quantity(ENERGY.name)
      .value(ENERGY_VALUE);
  }

  private Measurement.MeasurementBuilder tempMeasurement(LogicalMeter meter, ZonedDateTime date) {
    return measurement(meter)
      .created(date)
      .unit(DEGREES_CELSIUS)
      .quantity(EXTERNAL_TEMPERATURE.name)
      .value(TEMP_VALUE);
  }

  private Measurement.MeasurementBuilder humidityMeasurement(
    LogicalMeter meter,
    ZonedDateTime date
  ) {
    return measurement(meter)
      .created(date)
      .unit(PERCENT)
      .quantity(HUMIDITY.name)
      .value(HUMIDITY_VALUE);
  }

  private Measurement.MeasurementBuilder diffTempMeasurement(
    LogicalMeter meter,
    ZonedDateTime date
  ) {
    return measurement(meter).created(date)
      .unit(DEGREES_CELSIUS)
      .quantity(DIFFERENCE_TEMPERATURE.name)
      .value(DIFF_TEMP_VALUE_CELSIUS);
  }

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }
}
