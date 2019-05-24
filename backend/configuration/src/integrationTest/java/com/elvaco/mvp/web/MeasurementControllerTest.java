package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementRequestDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import org.assertj.core.data.Offset;
import org.assertj.core.util.DoubleComparator;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;
import static com.elvaco.mvp.core.domainmodels.DisplayMode.READOUT;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.domainmodels.Quantity.DIFFERENCE_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.EXTERNAL_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.HUMIDITY;
import static com.elvaco.mvp.core.domainmodels.Quantity.POWER;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.core.domainmodels.Units.DEGREES_CELSIUS;
import static com.elvaco.mvp.core.domainmodels.Units.KELVIN;
import static com.elvaco.mvp.core.domainmodels.Units.KILOWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.MEGAWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.PERCENT;
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
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?resolution=hour"
          + "&logicalMeterId=" + meter.id
          + "&quantity=Difference+temperature"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1),
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
    given(meter, measurement(meter).readoutTime(date)
      .unit(DEGREES_CELSIUS)
      .quantity(DIFFERENCE_TEMPERATURE.name)
      .value(DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementSeriesDto> measurements = asUser()
      .getList(
        "/measurements?quantity=Difference+temperature:K"
          + "&logicalMeterId=" + meter.id
          + "&resolution=hour"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1),
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
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementDto> measurements = asUser()
      .getList(
        "/measurements?logicalMeterId=" + meter.id
          + "&reportAfter=" + date
          + "&reportBefore=" + date,
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
    given(measurementSeries()
      .forMeter(firstOrganisationsMeter)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFF_TEMP_VALUE_CELSIUS));

    var secondOrganisationsMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(secondOrganisationsMeter)
      .startingAt(date)
      .withQuantity(DIFFERENCE_TEMPERATURE)
      .withValues(DIFF_TEMP_VALUE_CELSIUS));

    assertThat(getListAsSuperAdmin(
      "/measurements?logicalMeterId=" + firstOrganisationsMeter.id
        + "&logicalMeterId=" + secondOrganisationsMeter.id
        + "&quantity=Difference+temperature"
        + "&resolution=hour"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(1)
      )
    ).hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriodGet() {
    fetchMeasurementsForMeterByQuantityInPeriod(
      (measurementRequestDto) -> getListAsSuperAdmin("/measurements?"
        + "quantity=Difference+temperature"
        + "&logicalMeterId=" + measurementRequestDto.logicalMeterId.get(0)
        + "&reportAfter=" + measurementRequestDto.reportAfter
        + "&reportBefore=" + measurementRequestDto.reportBefore
        + "&resolution=" + measurementRequestDto.resolution.name())
    );
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriodPost() {
    fetchMeasurementsForMeterByQuantityInPeriod(
      (measurementRequestDto) -> asSuperAdmin().postList(
        "/measurements",
        measurementRequestDto,
        MeasurementSeriesDto.class
      ).getBody());
  }

  @Test
  public void fetchMeasurementsForMeterInPeriod() {
    ZonedDateTime date = context().now();

    LogicalMeter heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(heatMeter,
      measurement(heatMeter).readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter).readoutTime(date.plusHours(2))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "logicalMeterId=" + heatMeter.id
        + "&quantity=Difference+temperature,Energy"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(1)
        + "&resolution=hour");

    assertThat(contents)
      .contains(
        MeasurementSeriesDto.builder()
          .id(heatMeter.id.toString())
          .quantity("Difference temperature")
          .unit("K")
          .label(getExpectedLabel(heatMeter))
          .name(heatMeter.externalId)
          .meterId(getMeterId(heatMeter))
          .medium(MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name)
          .values(List.of(
            new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
            new MeasurementValueDto(date.plusHours(1).toInstant(), null)
          ))
          .build()
      );
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityInPeriodWithNonDefaultUnit() {
    ZonedDateTime date = context().now();

    LogicalMeter heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(heatMeter,
      measurement(heatMeter).readoutTime(date.minusHours(1))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter).readoutTime(date.plusHours(1))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?quantity=Difference+temperature:K"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(1)
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
    given(roomSensorMeter,
      measurement(roomSensorMeter)
        .readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(EXTERNAL_TEMPERATURE.name)
        .value(TEMP_VALUE),
      measurement(roomSensorMeter)
        .readoutTime(date)
        .unit(PERCENT)
        .quantity(HUMIDITY.name)
        .value(HUMIDITY_VALUE)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin("/measurements?"
        + "reportAfter=" + date
        + "&reportBefore=" + date
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
    given(heatMeter,
      measurement(heatMeter).readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter).readoutTime(date.plusHours(1))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter)
        .readoutTime(date)
        .unit("J")
        .quantity(ENERGY.name)
        .value(ENERGY_VALUE)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature:K,Energy:kWh"
          + "&logicalMeterId=" + heatMeter.id
          + "&resolution=hour"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1));

    assertThat(contents).containsExactlyInAnyOrder(
      MeasurementSeriesDto.builder()
        .id(heatMeter.id.toString())
        .quantity("Difference temperature")
        .unit("K")
        .label(getExpectedLabel(heatMeter))
        .name(heatMeter.externalId)
        .meterId(getMeterId(heatMeter))
        .medium(MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), DIFF_TEMP_VALUE_KELVIN),
          new MeasurementValueDto(date.plusHours(1).toInstant(), DIFF_TEMP_VALUE_KELVIN)
        ))
        .build(),
      MeasurementSeriesDto.builder()
        .id(heatMeter.id.toString())
        .quantity("Energy")
        .unit("kWh")
        .label(getExpectedLabel(heatMeter))
        .name(heatMeter.externalId)
        .meterId(getMeterId(heatMeter))
        .medium(MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), null),
          new MeasurementValueDto(date.plusHours(1).toInstant(), null)
        ))
        .build()
    );
  }

  @Test
  public void fetchConsumptionMeasurementsAsReadouts() {
    ZonedDateTime date = context().now();

    LogicalMeter meter = given(logicalMeter()
      .meterDefinition(DEFAULT_DISTRICT_HEATING));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(date)
      .withQuantity(ENERGY)
      .withValues(ENERGY_VALUE, ENERGY_VALUE));

    List<MeasurementSeriesDto> contents =
      asUser().getList(
        measurementsUrl()
          .quantity(ENERGY.name + ":kWh:readout")
          .logicalMeterId(meter.id)
          .resolution(TemporalResolution.hour)
          .reportPeriod(date, date.plusHours(1)).build(),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(contents).containsExactlyInAnyOrder(
      MeasurementSeriesDto.builder()
        .id(meter.id.toString())
        .quantity(ENERGY.name)
        .unit(ENERGY.storageUnit)
        .label(getExpectedLabel(meter))
        .name(meter.externalId)
        .meterId(getMeterId(meter))
        .medium(MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), ENERGY_VALUE),
          new MeasurementValueDto(date.plusHours(1).toInstant(), ENERGY_VALUE)
        ))
        .build()
    );
  }

  @Test
  public void fetchConsumptionMeasurementsAsConsumption() {
    ZonedDateTime date = context().now();

    LogicalMeter meter = given(logicalMeter()
      .meterDefinition(DEFAULT_DISTRICT_HEATING));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(date)
      .withQuantity(ENERGY)
      .withValues(ENERGY_VALUE, ENERGY_VALUE + 1));

    List<MeasurementSeriesDto> contents =
      asUser().getList(
        measurementsUrl()
          .quantity(ENERGY.name + ":kWh:consumption")
          .logicalMeterId(meter.id)
          .resolution(TemporalResolution.hour)
          .reportPeriod(date, date.plusHours(1)).build(),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(contents).containsExactlyInAnyOrder(
      MeasurementSeriesDto.builder()
        .id(meter.id.toString())
        .quantity(ENERGY.name)
        .unit(ENERGY.storageUnit)
        .label(getExpectedLabel(meter))
        .meterId(getMeterId(meter))
        .name(meter.externalId)
        .medium(MeterDefinition.DEFAULT_DISTRICT_HEATING.medium.name)
        .values(List.of(
          new MeasurementValueDto(date.toInstant(), 1.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), null)
        ))
        .build()
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

    given(meter11,
      measurement(meter11).readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(meter11)
        .readoutTime(date)
        .unit("J")
        .quantity(ENERGY.name)
        .value(ENERGY_VALUE)
    );

    given(meter12,
      measurement(meter12).readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(meter12)
        .readoutTime(date)
        .unit("J")
        .quantity(ENERGY.name)
        .value(ENERGY_VALUE)
    );

    given(meter2,
      measurement(meter2).readoutTime(date)
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(meter2)
        .readoutTime(date)
        .unit("J")
        .quantity(ENERGY.name)
        .value(ENERGY_VALUE)
    );

    List<MeasurementSeriesDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantity=Difference+temperature,Energy"
          + "&logicalMeterId=" + meter11.id
          + "&logicalMeterId=" + meter12.id
          + "&logicalMeterId=" + meter2.id
          + "&resolution=hour"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1));

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

    given(logicalMeter,
      measurement(logicalMeter).readoutTime(date)
      .unit(DEGREES_CELSIUS)
      .quantity(DIFFERENCE_TEMPERATURE.name)
      .value(DIFF_TEMP_VALUE_CELSIUS));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Difference+temperature"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=hour"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(1));

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.label).isEqualTo(getExpectedLabel(logicalMeter));
  }

  @Test
  public void fetchMeasurementsOverMeterReplacement() {
    ZonedDateTime date = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(date.minusDays(2), date)),
      physicalMeter().activePeriod(PeriodRange.from(date))
    );

    var physicalMeterOne = logicalMeter.activePhysicalMeter(date.minusDays(1)).get();
    var physicalMeterTwo = logicalMeter.activePhysicalMeter(date).get();
    var interval = Duration.ofDays(1);

    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterOne)
      .withQuantity(POWER)
      .startingAt(date.minusDays(2))
      .withInterval(interval)
      .withValues(2.0, 4.0, 6.0));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterTwo)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(interval)
      .withValues(8.0, 12.0));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Power"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=day"
        + "&reportAfter=" + date.minusDays(2)
        + "&reportBefore=" + date.plusDays(1));

    String labelForSerieOne = getExpectedLabel(logicalMeter, physicalMeterOne);
    String labelForSerieTwo = getExpectedLabel(logicalMeter, physicalMeterTwo);

    assertThat(contents)
      .extracting(
        dto -> dto.id,
        dto -> dto.label,
        dto -> dto.name,
        dto -> dto.meterId,
        dto -> dto.values
      )
      .containsExactlyInAnyOrder(
        tuple(
          logicalMeter.id.toString(),
          labelForSerieOne,
          logicalMeter.externalId,
          physicalMeterOne.address,
          List.of(
            new MeasurementValueDto(date.minusDays(2).toInstant(), 2.0),
            new MeasurementValueDto(date.minusDays(1).toInstant(), 4.0)
          )
        ),
        tuple(
          logicalMeter.id.toString(),
          labelForSerieTwo,
          logicalMeter.externalId,
          physicalMeterTwo.address,
          List.of(
            new MeasurementValueDto(date.toInstant(), 8.0),
            new MeasurementValueDto(date.plusDays(1).toInstant(), 12.0)
          )
        )
      );
  }

  @Test
  public void fetchConsumptionOverMeterReplacement() {
    ZonedDateTime date = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(date.minusDays(2), date)),
      physicalMeter().activePeriod(PeriodRange.from(date))
    );

    var physicalMeterOne = logicalMeter.activePhysicalMeter(date.minusDays(1)).get();
    var physicalMeterTwo = logicalMeter.activePhysicalMeter(date).get();
    var interval = Duration.ofDays(1);

    given(measurementSeries()
      .forPhysicalMeter(physicalMeterOne)
      .forMeter(logicalMeter)
      .withQuantity(ENERGY)
      .startingAt(date.minusDays(2))
      .withInterval(interval)
      .withValues(2.0, 4.0, 7.0));
    given(measurementSeries()
      .forPhysicalMeter(physicalMeterTwo)
      .forMeter(logicalMeter)
      .withQuantity(ENERGY)
      .startingAt(date)
      .withInterval(interval)
      .withValues(8.0, 12.0, 17.0));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Energy"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=day"
        + "&reportAfter=" + date.minusDays(2)
        + "&reportBefore=" + date.plusDays(1));

    String labelForSerieOne = getExpectedLabel(logicalMeter, physicalMeterOne);
    String labelForSerieTwo = getExpectedLabel(logicalMeter, physicalMeterTwo);

    assertThat(contents)
      .extracting(
        dto -> dto.id,
        dto -> dto.label,
        dto -> dto.name,
        dto -> dto.meterId,
        dto -> dto.values
      )
      .containsExactlyInAnyOrder(
        tuple(
          logicalMeter.id.toString(),
          labelForSerieOne,
          logicalMeter.externalId,
          physicalMeterOne.address,
          List.of(
            new MeasurementValueDto(date.minusDays(2).toInstant(), 2.0),
            new MeasurementValueDto(date.minusDays(1).toInstant(), null)
          )
        ),
        tuple(
          logicalMeter.id.toString(),
          labelForSerieTwo,
          logicalMeter.externalId,
          physicalMeterTwo.address,
          List.of(
            new MeasurementValueDto(date.toInstant(), 4.0),
            new MeasurementValueDto(date.plusDays(1).toInstant(), 5.0)
          )
        )
      );
  }

  @Test
  public void fetchMeasurementOutsideSchedule_limitNrOfMeters() {
    ZonedDateTime date = context().now();

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Power"
          + logicalMeterIdRequestString(11)
          + "&resolution=all"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusDays(1),
        ErrorMessageDto.class
      );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message)
      .isEqualTo("Scope of period length and meters is too large for this resolution");
  }

  @Test
  public void fetchMeasurementOutsideSchedule_limitNrOfDays() {
    ZonedDateTime date = context().now();

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Power"
          + logicalMeterIdRequestString(1)
          + "&resolution=all"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusDays(12),
        ErrorMessageDto.class
      );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message)
      .isEqualTo("Scope of period length and meters is too large for this resolution");
  }

  @Test
  public void fetchMeasurementOutsideSchedule() {
    ZonedDateTime date = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter()
        .readIntervalMinutes(60)
        .activePeriod(PeriodRange.from(date))
    );

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(7))
      .withValues(1.0, 2.0, 3.0));

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(POWER)
      .startingAt(date.plusHours(1))
      .withInterval(Duration.ofHours(1))
      .withValues(10.0, 11.0));

    given(logicalMeter, measurement(logicalMeter)
      .quantity(POWER.name)
      .unit(Units.WATT)
      .readoutTime(date.plusNanos(370000000))
      .value(370.0));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Power"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=all"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(2));

    assertThat(contents)
      .flatExtracting(dto -> dto.values)
      .extracting(v -> v.when, v -> v.value)
      .containsExactly(
        tuple(date.toInstant(), 1.0),
        tuple(date.plusNanos(370000000).toInstant(), 370.0),
        tuple(date.plusSeconds(7).toInstant(), 2.0),
        tuple(date.plusSeconds(14).toInstant(), 3.0),
        tuple(date.plusHours(1).toInstant(), 10.0),
        tuple(date.plusHours(2).toInstant(), 11.0)
      );
  }

  @Test
  public void fetchConsumptionOutsideSchedule() {
    ZonedDateTime date = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter()
        .readIntervalMinutes(60)
        .activePeriod(PeriodRange.from(date))
    );

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(ENERGY)
      .startingAt(date)
      .withInterval(Duration.ofSeconds(7))
      .withValues(1.0, 2.0, 3.0));

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(ENERGY)
      .startingAt(date.plusHours(1))
      .withInterval(Duration.ofHours(1))
      .withValues(10.0, 15.0));

    given(logicalMeter, measurement(logicalMeter)
      .quantity(ENERGY.name)
      .unit(Units.KILOWATT_HOURS)
      .readoutTime(date.plusNanos(370000000))
      .value(1.7));

    List<MeasurementSeriesDto> contents = getListAsSuperAdmin(
      "/measurements?quantity=Energy"
        + "&logicalMeterId=" + logicalMeter.id.toString()
        + "&resolution=all"
        + "&reportAfter=" + date
        + "&reportBefore=" + date.plusHours(2));

    assertThat(contents)
      .flatExtracting(dto -> dto.values)
      .extracting(v -> v.value)
      .usingComparatorForType(new DoubleComparator(0.02), Double.class)
      .containsExactly(
        0.7,
        0.3,
        1.0,
        7.0,
        5.0,
        null
      );

    assertThat(contents)
      .flatExtracting(dto -> dto.values)
      .extracting(v -> v.when)
      .containsExactly(
        date.toInstant(),
        date.plusNanos(370000000).toInstant(),
        date.plusSeconds(7).toInstant(),
        date.plusSeconds(14).toInstant(),
        date.plusHours(1).toInstant(),
        date.plusHours(2).toInstant()
      );
  }

  @Test
  public void unknownUnitSuppliedForScaling() {
    ZonedDateTime date = context().now();
    LogicalMeter heatMeter = given(
      logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
    );

    given(heatMeter,
      measurement(heatMeter).readoutTime(date)
      .unit(DEGREES_CELSIUS)
      .quantity(DIFFERENCE_TEMPERATURE.name)
      .value(DIFF_TEMP_VALUE_CELSIUS));

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:unknownUnit"
          + "&logicalMeterId=" + heatMeter.id.toString()
          + "&resolution=hour"
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1),
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

    given(heatMeter,
      measurement(heatMeter).readoutTime(date)
      .unit(DEGREES_CELSIUS)
      .quantity(DIFFERENCE_TEMPERATURE.name)
      .value(DIFF_TEMP_VALUE_CELSIUS));

    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(
        "/measurements?quantity=Difference+temperature:kWh"
          + "&logicalMeterId=" + heatMeter.id
          + "&reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1)
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
    assertThat(response.getBody().message).isEqualTo("Missing 'reportAfter' parameter.");
  }

  @Test
  public void consumptionSeriesIsDisplayedWithConsumptionValuesAtFirstTimeInInterval() {
    ZonedDateTime when = context().now();
    var consumptionMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(measurementSeries()
        .forMeter(consumptionMeter)
        .withQuantity(VOLUME)
        .startingAt(context().now())
        .withValues(25, 35, 55)
    );

    List<MeasurementSeriesDto> list = asUser()
      .getList(
        "/measurements?resolution=hour&quantity=Volume"
          + "&logicalMeterId=" + consumptionMeter.id
          + "&reportAfter=" + when
          + "&reportBefore=" + when.plusHours(2),
        MeasurementSeriesDto.class
      ).getBody();

    assertThat(list).containsExactly(
      MeasurementSeriesDto.builder()
        .id(consumptionMeter.id.toString())
        .quantity(VOLUME.name)
        .unit(VOLUME.storageUnit)
        .name(consumptionMeter.externalId)
        .meterId(getMeterId(consumptionMeter))
        .label(getExpectedLabel(consumptionMeter))
        .medium(consumptionMeter.meterDefinition.medium.name)
        .values(List.of(
          new MeasurementValueDto(when.toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 20.0),
          new MeasurementValueDto(when.plusHours(2).toInstant(), null)
        ))
        .build()
    );
  }

  @Test
  public void consumptionIsIncludedForValueDirectAfterPeriod() {
    ZonedDateTime when = context().now();
    var consumptionMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(measurementSeries()
      .forMeter(consumptionMeter)
      .withQuantity(VOLUME)
      .startingAt(context().now())
      .withValues(25, 35, 55));

    List<MeasurementSeriesDto> seriesDto = asUser()
      .getList(String.format(
        "/measurements?resolution=hour&quantity=Volume&logicalMeterId=%s"
          + "&reportAfter=%s&reportBefore=%s",
        consumptionMeter.id,
        when,
        when.plusHours(1)
      ), MeasurementSeriesDto.class).getBody();

    assertThat(seriesDto).containsExactly(
      MeasurementSeriesDto.builder()
        .id(consumptionMeter.id.toString())
        .quantity("Volume")
        .unit("m³")
        .label(getExpectedLabel(consumptionMeter))
        .name(consumptionMeter.externalId)
        .meterId(getMeterId(consumptionMeter))
        .medium(consumptionMeter.meterDefinition.medium.name)
        .values(List.of(
          new MeasurementValueDto(when.plusHours(0).toInstant(), 10.0),
          new MeasurementValueDto(when.plusHours(1).toInstant(), 20.0)
        ))
        .build()
    );
  }

  @Test
  public void findsConsumptionForGasMeters() {
    ZonedDateTime when = context().now();
    var logicalMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));

    given(measurementSeries()
        .forMeter(logicalMeter)
        .withQuantity(VOLUME)
        .startingAt(context().now())
        .withValues(1, 2, 5)
    );

    List<MeasurementSeriesDto> response = asUser()
      .getList(String.format(
        "/measurements"
          + "?reportAfter=" + when
          + "&reportBefore=" + when.plusHours(2)
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
          + "?reportAfter=" + after
          + "&reportBefore=" + before
          + "&quantity=Floop"
          + "&logicalMeterId=" + logicalMeter.id, MeasurementSeriesDto.class).getBody();

    assertThat(response).hasSize(0);
  }

  @Test
  public void measurementsWithNonExpectedMeasurementAtMeterActivePeriodStart() {
    var activePeriodStart = context().now().plusMinutes(1);

    var meter = given(
      logicalMeter()
        .meterDefinition(DEFAULT_DISTRICT_HEATING),
      physicalMeter()
        .activePeriod(PeriodRange.halfOpenFrom(activePeriodStart, null))
        .readIntervalMinutes(60)
    );

    // We need to create measurements before the meter's active period starts
    PhysicalMeter physicalMeter = meter.activePhysicalMeter().orElseThrow();

    given(measurementSeries()
      .forMeter(meter)
      .forPhysicalMeter(physicalMeter)
        .withQuantity(POWER)
        .startingAt(context().now())
        .withValues(
          1.0, //+00:00 (before meter became active)
          2.0, //+01:00
          3.0  //+02:00
        )
    );

    given(measurementSeries()
        .forMeter(meter)
        .forPhysicalMeter(physicalMeter)
        .withQuantity(POWER)
        .startingAt(activePeriodStart)
        .withValues(9999.0)
    );

    List<MeasurementSeriesDto> response = asUser()
      .getList(measurementsUrl()
        .reportPeriod(context().now(), context().now().plusHours(2))
        .quantity(POWER)
        .resolution(TemporalResolution.hour)
        .logicalMeterId(meter.id), MeasurementSeriesDto.class).getBody();

    assertThat(response.size()).isEqualTo(1);

    MeasurementSeriesDto seriesDto = response.get(0);

    assertThat(seriesDto.values).containsExactly(
      //1.0 is not expected, since the meter is not active at that point
      // 9999.0 is not expected, since it does not coincide with the meter's read interval
      new MeasurementValueDto(context().now().plusHours(1).toInstant(), 2.0),
      new MeasurementValueDto(context().now().plusHours(2).toInstant(), 3.0)
    );
  }

  @Test
  public void allowsOverridingDefinitionsDisplayMode() {
    var date = context().now();
    var logicalMeter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(VOLUME)
      .startingAt(context().now())
      .withValues(40000.0, 40010.0));

    assertThat(logicalMeter.getQuantity(VOLUME.name).get().storageMode).isEqualTo(CONSUMPTION);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(String.format(
        "/measurements"
          + "?reportAfter=" + date
          + "&reportBefore=" + date.plusHours(1)
          + "&quantity=" + VOLUME.name + "::" + READOUT.name()
          + "&logicalMeterId=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
      .extracting("quantity")
      .containsExactly(VOLUME.name);

    assertThat(response.getBody())
      .extracting(m -> m.quantity, m -> m.values)
      .containsExactly(tuple(
        VOLUME.name,
        List.of(
          new MeasurementValueDto(date.toInstant(), 40000.0),
          new MeasurementValueDto(date.plusHours(1).toInstant(), 40010.0)
        )
      ));
  }

  public void fetchMeasurementsForMeterByQuantityInPeriod(
    Function<MeasurementRequestDto, List<MeasurementSeriesDto>> requestFunction
  ) {
    ZonedDateTime date = context().now();

    var heatMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(
      measurement(heatMeter).created(date.minusHours(1))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter).created(date.plusHours(1))
        .unit(DEGREES_CELSIUS)
        .quantity(DIFFERENCE_TEMPERATURE.name)
        .value(DIFF_TEMP_VALUE_CELSIUS),
      measurement(heatMeter)
        .created(date.plusHours(1))
        .unit("J")
        .quantity(ENERGY.name)
        .value(ENERGY_VALUE)
    );

    MeasurementRequestDto measurementRequestDto = new MeasurementRequestDto(
      List.of(heatMeter.id),
      date,
      date.plusHours(1),
      Set.of(QuantityParameter.of(DIFFERENCE_TEMPERATURE.name)),
      TemporalResolution.hour
    );

    List<MeasurementSeriesDto> contents = requestFunction.apply(measurementRequestDto);

    assertThat(contents).hasSize(1);
    MeasurementSeriesDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Difference temperature");
    assertThat(dto.values).hasSize(2);
    assertThat(dto.values.get(0).value).isNull();
    assertThat(dto.values.get(1).value).isEqualTo(DIFF_TEMP_VALUE_KELVIN, OFFSET);
  }

  private String logicalMeterIdRequestString(int numberOfMeters) {
    StringBuffer sb = new StringBuffer();
    IntStream.rangeClosed(1, numberOfMeters)
      .boxed()
      .map(i -> given(logicalMeter()))
      .forEach(lm -> sb.append("&logicalMeterId=").append(lm.id.toString()));
    return sb.toString();
  }

  private List<MeasurementSeriesDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementSeriesDto.class).getBody();
  }

  private static Url.UrlBuilder measurementsUrl() {
    return Url.builder().path("/measurements");
  }

  private static String getExpectedLabel(LogicalMeter meter) {
    assertThat(meter.physicalMeters.size()).isEqualTo(1);
    return meter.externalId + "-" + getMeterId(meter);
  }

  private static String getExpectedLabel(LogicalMeter logicalMeter, PhysicalMeter physicalMeter) {
    return logicalMeter.externalId + "-" + physicalMeter.address;
  }

  private static String getMeterId(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.get(0).address;
  }
}
