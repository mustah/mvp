package com.elvaco.mvp.access;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementStatTest extends IntegrationTest {

  private static final ZonedDateTime TIME = ZonedDateTime.parse("2018-11-22T01:00:00+01")
    .truncatedTo(ChronoUnit.HOURS);

  private static final ZonedDateTime UTC_TIME = ZonedDateTime.parse("2018-11-23T00:00:00+00")
    .truncatedTo(ChronoUnit.HOURS);

  @Autowired
  private DSLContext dsl;

  private MeasurementStatData statData = MeasurementStatData.MEASUREMENT_STAT_DATA;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
  }

  @Test
  public void insertUpdatesStats() {
    PhysicalMeter meter = newConnectedMeter();
    Measurement measurement = measurements.save(
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    MeasurementStatDto stat = fetchMeasurementStats();

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
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      measurementFor(meter)
        .value(8.0)
        .created(TIME.plusHours(1))
        .build()
    );

    MeasurementStatDto stat = fetchMeasurementStats();

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
      measurementFor(meter)
        .value(8.0)
        .created(UTC_TIME.plusHours(i))
        .build())
    );

    List<MeasurementStatDto> result = dsl.select()
      .from(statData).orderBy(statData.STAT_DATE.asc())
      .fetchInto(MeasurementStatDto.class);

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
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      measurementFor(meter)
        .value(8.0)
        .created(TIME.plusHours(1))
        .build()
    );

    measurementJpaRepository.delete(MeasurementEntityMapper.toEntity(measurement));

    MeasurementStatDto stat = fetchMeasurementStats();

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
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    measurements.save(
      measurementFor(meter)
        .created(TIME)
        .value(2.0)
        .build()
    );

    MeasurementStatDto stat = fetchMeasurementStats();

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
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    measurementJpaRepository.delete(MeasurementEntityMapper.toEntity(measurement));

    assertThat(dsl.select()
      .from(statData)
      .fetchInto(MeasurementStatDto.class)
    ).hasSize(0);
  }

  @Test
  public void insertMeasurementForZeroIntervalMeterIsOk() {
    PhysicalMeter meter = newConnectedMeter(0);

    Measurement measurement = measurements.save(
      measurementFor(meter).created(TIME).value(1.0).build()
    );

    MeasurementStatDto stat = fetchMeasurementStats();

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

  private Measurement.MeasurementBuilder measurementFor(PhysicalMeter meter) {
    return Measurement.builder()
      .quantity(Quantity.POWER.name)
      .unit(Quantity.POWER.storageUnit)
      .physicalMeter(meter);
  }

  private MeasurementStatDto fetchMeasurementStats() {
    return dsl.select()
      .from(statData)
      .fetchOneInto(MeasurementStatDto.class);
  }

  private MeasurementStatDto.MeasurementStatDtoBuilder newStatDto(Measurement measurement) {
    return MeasurementStatDto.builder()
      .date(measurement.created.toLocalDate())
      .expectedCount(24)
      .quantityId(QuantityAccess.singleton().getId(measurement.getQuantity()))
      .physicalMeterId(measurement.physicalMeter.id);
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
        .id(logicalMeterId)
        .utcOffset(utcOffset)
        .build()
    );

    return physicalMeters.save(PhysicalMeter.builder()
      .manufacturer("ELV")
      .id(UUID.randomUUID())
      .externalId(logicalMeterId.toString())
      .organisation(context().organisation())
      .address("1234")
      .logicalMeterId(logicalMeterId)
      .readIntervalMinutes(readIntervalMinutes)
      .build());
  }

  private PhysicalMeter newConnectedMeter(int readIntervalMinutes) {
    return newConnectedMeter(readIntervalMinutes, "+01");
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
    public final double expectedCount;
    public final double receivedCount;
    public final double average;
  }
}
