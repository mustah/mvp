package com.elvaco.mvp.access;

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

  private static final ZonedDateTime TIME = ZonedDateTime.parse("2018-11-22T01:00:00+01")
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
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
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
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      powerMeasurementFor(meter)
        .value(8.0)
        .created(TIME.plusHours(1))
        .build()
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
    PhysicalMeter meter = newConnectedMeter(60, "+05");

    IntStream.range(0, 25).forEach(i -> measurements.save(
      powerMeasurementFor(meter)
        .value(8.0)
        .created(UTC_TIME.plusHours(i))
        .build())
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
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      powerMeasurementFor(meter)
        .value(8.0)
        .created(TIME.plusHours(1))
        .build()
    );

    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

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
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      powerMeasurementFor(meter)
        .created(TIME)
        .value(2.0)
        .build()
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
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
    );

    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

    assertThat(dsl.select()
      .from(statData)
      .fetchInto(MeasurementStatDto.class)
    ).hasSize(0);
  }

  @Test
  public void insertMeasurementForZeroIntervalMeterIsOk() {
    PhysicalMeter meter = newConnectedMeter(0);

    Measurement measurement = measurements.save(
      powerMeasurementFor(meter).created(TIME).value(1.0).build()
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
    PhysicalMeter meter = newConnectedMeter();

    IntStream.range(0, 25).forEach(i -> measurements.save(
      volumeMeasurementFor(meter)
        .value((double) i)
        .created(UTC_TIME.plusHours(i - 1))
        .build())
    );

    List<MeasurementStatDto> result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).receivedCount).isEqualTo(24);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }

  @Test
  public void consumptionFor24hUnknownMeter() {
    PhysicalMeter meter = newUnknownMeter();

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter)
        .value((double) i)
        .created(UTC_TIME.plusHours(i - 1))
        .build())
    );

    List<MeasurementStatDto> result = fetchMeasurementStats();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).receivedCount).isEqualTo(24);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }
  
  @Test
  public void consumptionAddInAGapFor24h() {
    PhysicalMeter meter = newConnectedMeter(60, "+00:00");

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter)
        .value((double) i)
        .created(UTC_TIME.plusHours(i))
        .build())
    );

    IntStream.range(0, 24).forEach(i -> measurements.save(
      volumeMeasurementFor(meter)
        .value((double) i + 49)
        .created(UTC_TIME.plusDays(2).plusHours(1 + i))
        .build())
    );

    List<MeasurementStatDto> result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).receivedCount).isEqualTo(23);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(1).receivedCount).isEqualTo(24);
    assertThat(result.get(1).max).isEqualTo(26);
    assertThat(result.get(1).date).isEqualTo(UTC_TIME.plusDays(2)
      .truncatedTo(ChronoUnit.DAYS).toLocalDate());

    measurements.save(
      volumeMeasurementFor(meter)
        .value((double) 48)
        .created(UTC_TIME.plusDays(2))
        .build());
    result = fetchConsumptionMeasurementStats();

    assertThat(result).hasSize(3);
    assertThat(result.get(0).receivedCount).isEqualTo(23);
    assertThat(result.get(0).date).isEqualTo(UTC_TIME.truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(1).receivedCount).isEqualTo(1);
    assertThat(result.get(1).max).isEqualTo(25);
    assertThat(result.get(1).date).isEqualTo(UTC_TIME.plusDays(1)
      .truncatedTo(ChronoUnit.DAYS).toLocalDate());
    assertThat(result.get(2).receivedCount).isEqualTo(24);
    assertThat(result.get(2).max).isEqualTo(1);
    assertThat(result.get(2).date).isEqualTo(UTC_TIME.plusDays(2)
      .truncatedTo(ChronoUnit.DAYS).toLocalDate());
  }

  @Test
  public void consumptionDeleteOne() {
    PhysicalMeter meter = newConnectedMeter();
    measurements.save(
      volumeMeasurementFor(meter)
        .value((double) 1)
        .created(UTC_TIME)
        .build());
    Measurement measurement = measurements.save(
      volumeMeasurementFor(meter)
        .value((double) 1)
        .created(UTC_TIME.plusHours(1))
        .build());
    measurementJpaRepository.delete(measurementEntityMapper.toEntity(measurement));

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
    return dsl.select()
      .from(statData)
      .where(statData.IS_CONSUMPTION.isTrue())
      .orderBy(statData.STAT_DATE.asc())
      .fetchInto(MeasurementStatDto.class);
  }

  private List<MeasurementStatDto> fetchMeasurementStats() {
    return dsl.select()
      .from(statData).orderBy(statData.STAT_DATE.asc())
      .fetchInto(MeasurementStatDto.class);
  }

  private MeasurementStatDto.MeasurementStatDtoBuilder newStatDto(Measurement measurement) {
    return MeasurementStatDto.builder()
      .date(measurement.created.toLocalDate())
      .expectedCount(24)
      .quantityId(quantityProvider.getId(measurement.getQuantity()))
      .physicalMeterId(measurement.physicalMeter.id);
  }

  private PhysicalMeter newUnknownMeter() {
    UUID logicalMeterId = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
        .organisationId(context().organisationId())
        .externalId(logicalMeterId.toString())
        .meterDefinition(MeterDefinition.UNKNOWN)
        .id(logicalMeterId)
        .utcOffset("+01")
        .build());
    return physicalMeters.save(PhysicalMeter.builder()
      .manufacturer("ELV")
      .id(UUID.randomUUID())
      .externalId(logicalMeterId.toString())
      .organisationId(context().organisationId())
      .address("1234")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(60)
      .build());
  }

  private PhysicalMeter newConnectedMeter() {
    return newConnectedMeter(60);
  }

  private PhysicalMeter newConnectedMeter(int readIntervalMinutes, String utcOffset) {
    UUID logicalMeterId = UUID.randomUUID();
    logicalMeters.save(
      LogicalMeter.builder()
        .organisationId(context().organisationId())
        .externalId(logicalMeterId.toString())
        .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
        .id(logicalMeterId)
        .utcOffset(utcOffset)
        .build()
    );

    return physicalMeters.save(PhysicalMeter.builder()
      .manufacturer("ELV")
      .id(UUID.randomUUID())
      .externalId(logicalMeterId.toString())
      .organisationId(context().organisationId())
      .address("1234")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(readIntervalMinutes)
      .build());
  }

  private PhysicalMeter newConnectedMeter(int readIntervalMinutes) {
    return newConnectedMeter(readIntervalMinutes, LogicalMeter.UTC_OFFSET);
  }

  @RequiredArgsConstructor
  @EqualsAndHashCode
  @Builder
  @ToString
  static class MeasurementStatDto {
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
}
