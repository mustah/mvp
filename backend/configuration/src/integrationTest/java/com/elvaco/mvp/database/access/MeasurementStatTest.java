package com.elvaco.mvp.database.access;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.jooq.tables.MeasurementStatData;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jooq.DSLContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class MeasurementStatTest extends IntegrationTest {

  private static final ZonedDateTime TIME = ZonedDateTime.parse("2018-11-22T00:00:00+01")
    .truncatedTo(ChronoUnit.HOURS);

  private static final ZonedDateTime UTC_TIME = ZonedDateTime.parse("2018-11-23T00:00:00+00")
    .truncatedTo(ChronoUnit.HOURS);

  private final MeasurementStatData statData = MeasurementStatData.MEASUREMENT_STAT_DATA;

  @Autowired
  private QuantityProvider quantityProvider;

  @Autowired
  private DSLContext dsl;

  @Autowired
  private MeasurementEntityMapper measurementEntityMapper;

  @Test
  public void insertUpdatesStats() {
    Meters meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter).readoutTime(TIME).value(1.0).build(),
      meter.logicalMeter
    );

    MeasurementStatDto stat = fetchMeasurementStats().get(0);

    assertThat(stat).isEqualTo(
      newStatDto(measurement)
        .average(1.0)
        .expectedCount(24)
        .receivedCount(1)
        .min(1.0)
        .max(1.0)
        .build()
    );
  }

  @Test
  public void insertMultipleForDay_StatsAreCorrect() {
    Meters meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter).readoutTime(TIME).value(1.0).build(),
      meter.logicalMeter
    );

    measurements.save(
      powerMeasurementFor(meter.physicalMeter)
        .value(8.0)
        .readoutTime(TIME.plusHours(1))
        .build(),
      meter.logicalMeter
    );

    MeasurementStatDto stat = fetchMeasurementStats().get(0);

    assertThat(stat).isEqualTo(
      newStatDto(measurement)
        .max(8.0)
        .min(1.0)
        .average(4.5)
        .receivedCount(2)
        .build()
    );
  }

  @Test
  public void respectsMeterTimeZone() {
    Meters meter = newConnectedMeter(60, "+05");

    IntStream.range(0, 25).forEach(i -> measurements.save(
      powerMeasurementFor(meter.physicalMeter)
        .value(8.0)
        .readoutTime(UTC_TIME.plusHours(i))
        .build(),
      meter.logicalMeter)
    );

    List<MeasurementStatDto> result = fetchMeasurementStats();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).receivedCount).isEqualTo(19);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(1).receivedCount).isEqualTo(6);
    assertThat(result.get(1).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).plusDays(1)
      .toLocalDate());
  }

  @Test
  public void deleteUpdatesStats() {
    Meters meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter)
        .readoutTime(TIME)
        .value(1.0).build(),
      meter.logicalMeter
    );

    measurements.save(
      powerMeasurementFor(meter.physicalMeter)
        .value(8.0)
        .readoutTime(TIME.plusHours(1))
        .build(),
      meter.logicalMeter
    );
    waitForMeasurementStat();
    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

    waitForMeasurementStat();

    MeasurementStatDto stat = fetchMeasurementStats().get(0);

    assertThat(stat).isEqualTo(
      newStatDto(measurement)
        .max(8.0)
        .min(8.0)
        .average(8.0)
        .receivedCount(1)
        .build()
    );
  }

  @Test
  public void updateUpdatesStats() {
    Meters meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter).readoutTime(TIME).value(1.0).build(),
      meter.logicalMeter
    );

    measurements.save(
      powerMeasurementFor(meter.physicalMeter)
        .readoutTime(TIME)
        .value(2.0)
        .build(),
      meter.logicalMeter
    );

    MeasurementStatDto stat = fetchMeasurementStats().get(0);

    assertThat(stat).isEqualTo(
      newStatDto(measurement)
        .average(2.0)
        .expectedCount(24)
        .receivedCount(1)
        .min(2.0)
        .max(2.0)
        .build()
    );
  }

  @Test
  public void deleteLastMeasurementForDayDeletesStats() {
    Meters meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter).readoutTime(TIME).value(1.0).build(),
      meter.logicalMeter
    );

    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

    waitForMeasurementStat();

    assertThat(dsl.select()
      .from(statData)
      .fetchInto(MeasurementStatDto.class)
    ).hasSize(0);
  }

  @Test
  public void insertMeasurementForZeroIntervalMeterIsOk() {
    Meters meter = newConnectedMeter(0);

    Measurement measurement = measurements.save(
      powerMeasurementFor(meter.physicalMeter).readoutTime(TIME).value(1.0).build(),
      meter.logicalMeter
    );

    MeasurementStatDto stat = fetchMeasurementStats().get(0);

    assertThat(stat).isEqualTo(
      newStatDto(measurement)
        .average(1.0)
        .expectedCount(0)
        .receivedCount(1)
        .min(1.0)
        .max(1.0)
        .build()
    );
  }

  @Test
  public void consumptionFor24h() {
    Meters meter = newConnectedMeter();

    IntStream.range(0, 25).forEach(i -> measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) i)
        .readoutTime(UTC_TIME.plusHours(i - 1))
        .build(),
      meter.logicalMeter)
    );

    List<MeasurementStatDto> result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).receivedCount).isEqualTo(24);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }

  @Test
  public void consumptionFor24hUnknownMeter() {
    Meters meter = newUnknownMeter();

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) i)
        .readoutTime(UTC_TIME.plusHours(i - 1))
        .build(),
      meter.logicalMeter)
    );

    List<MeasurementStatDto> result = fetchMeasurementStats();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).receivedCount).isEqualTo(24);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }
  
  @Test
  public void consumptionAddInAGapFor24h() {
    Meters meter = newConnectedMeter(60, "+00:00");

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) i)
        .readoutTime(UTC_TIME.plusHours(i))
        .build(),
      meter.logicalMeter)
    );

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) i + 49)
        .readoutTime(UTC_TIME.plusDays(2).plusHours(1 + i))
        .build(),
      meter.logicalMeter)
    );
    waitForMeasurementStat();
    List<MeasurementStatDto> result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).receivedCount).isEqualTo(23);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(1).receivedCount).isEqualTo(23);
    assertThat(result.get(1).max).isEqualTo(1);
    assertThat(result.get(1).date).isEqualTo(UTC_TIME.plusDays(2)
      .truncatedTo(ChronoUnit.DAYS).toLocalDate());

    measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) 48)
        .readoutTime(UTC_TIME.plusDays(2))
        .build(),
      meter.logicalMeter);
    waitForMeasurementStat();
    result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).receivedCount).isEqualTo(23);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(1).receivedCount).isEqualTo(24);
    assertThat(result.get(1).max).isEqualTo(1);
    assertThat(result.get(1).date).isEqualTo(UTC_TIME.plusDays(2)
      .truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }

  @Test
  public void consumptionDeleteOne() {
    Meters meter = newConnectedMeter();
    measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) 1)
        .readoutTime(UTC_TIME)
        .build(),
      meter.logicalMeter);
    Measurement measurement = measurements.save(
      volumeMeasurementFor(meter.physicalMeter)
        .value((double) 1)
        .readoutTime(UTC_TIME.plusHours(1))
        .build(),
      meter.logicalMeter);
    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

    waitForMeasurementStat();

    assertThat(fetchConsumptionMeasurementStats()).hasSize(0);
  }

  @Test
  public void consumptionFor24hReadInterval() {
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(24 * 60));
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.VOLUME)
      .startingAt(context().now())
      .withValues(1, 2, 3));
    var result = fetchConsumptionMeasurementStats();

    assertThat(result)
      .extracting(
        r -> r.min,
        r -> r.max,
        r -> r.average,
        r -> r.expectedCount,
        r -> r.receivedCount
      )
      .containsExactly(
        tuple(1.0, 1.0, 1.0, 1, 1),
        tuple(1.0, 1.0, 1.0, 1, 1)
      );
  }

  private Measurement.MeasurementBuilder powerMeasurementFor(PhysicalMeter meter) {
    return measurementFor(meter, Quantity.POWER);
  }

  private Measurement.MeasurementBuilder volumeMeasurementFor(PhysicalMeter meter) {
    return measurementFor(meter, Quantity.VOLUME);
  }

  private Measurement.MeasurementBuilder measurementFor(PhysicalMeter meter, Quantity qty) {
    return Measurement.builder()
      .quantity(qty.name)
      .unit(qty.storageUnit)
      .physicalMeter(meter);
  }

  private List<MeasurementStatDto> fetchConsumptionMeasurementStats() {
    waitForMeasurementStat();
    return dsl.select()
      .from(statData)
      .where(statData.IS_CONSUMPTION.isTrue())
      .orderBy(statData.STAT_DATE.asc())
      .fetchInto(MeasurementStatDto.class);
  }

  private List<MeasurementStatDto> fetchMeasurementStats() {
    waitForMeasurementStat();
    return dsl.select()
      .from(statData).orderBy(statData.STAT_DATE.asc())
      .fetchInto(MeasurementStatDto.class);
  }

  private MeasurementStatDto.MeasurementStatDtoBuilder newStatDto(Measurement measurement) {
    return MeasurementStatDto.builder()
      .date(measurement.readoutTime.toLocalDate())
      .expectedCount(24)
      .quantityId(quantityProvider.getId(measurement.getQuantity()))
      .physicalMeterId(measurement.physicalMeter.id)
      .organisationId(measurement.physicalMeter.organisationId);
  }

  private Meters newUnknownMeter() {
    Meters meters = new Meters();
    UUID logicalMeterId = UUID.randomUUID();
    meters.logicalMeter = logicalMeters.save(LogicalMeter.builder()
        .organisationId(context().organisationId())
        .externalId(logicalMeterId.toString())
        .meterDefinition(MeterDefinition.UNKNOWN)
        .id(logicalMeterId)
        .utcOffset("+01")
        .build());
    meters.physicalMeter = physicalMeters.save(PhysicalMeter.builder()
      .manufacturer("ELV")
      .id(UUID.randomUUID())
      .externalId(logicalMeterId.toString())
      .organisationId(context().organisationId())
      .address("1234")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(60)
      .activePeriod(PeriodRange.from(UTC_TIME.minusHours(1)))
      .build());
    return meters;
  }

  private Meters newConnectedMeter() {
    return newConnectedMeter(60);
  }

  private Meters newConnectedMeter(int readIntervalMinutes, String utcOffset) {
    Meters meters = new Meters();
    UUID logicalMeterId = UUID.randomUUID();
    meters.logicalMeter = logicalMeters.save(
      LogicalMeter.builder()
        .organisationId(context().organisationId())
        .externalId(logicalMeterId.toString())
        .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
        .id(logicalMeterId)
        .utcOffset(utcOffset)
        .build()
    );

    meters.physicalMeter = physicalMeters.save(PhysicalMeter.builder()
      .manufacturer("ELV")
      .id(UUID.randomUUID())
      .externalId(logicalMeterId.toString())
      .organisationId(context().organisationId())
      .address("1234")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(readIntervalMinutes)
      .activePeriod(PeriodRange.from(TIME))
      .build());
    return meters;
  }

  private Meters newConnectedMeter(int readIntervalMinutes) {
    return newConnectedMeter(readIntervalMinutes, LogicalMeter.UTC_OFFSET);
  }

  @RequiredArgsConstructor
  @EqualsAndHashCode
  @Builder
  @ToString
  static class MeasurementStatDto {
    public final UUID organisationId;
    public final LocalDate date;
    public final UUID physicalMeterId;
    public final int quantityId;
    public final double min;
    public final double max;
    public final int expectedCount;
    public final int receivedCount;
    public final double average;
    public final boolean isConsumption;
  }

  private static class Meters {
    public PhysicalMeter physicalMeter;
    public LogicalMeter logicalMeter;
  }
}
