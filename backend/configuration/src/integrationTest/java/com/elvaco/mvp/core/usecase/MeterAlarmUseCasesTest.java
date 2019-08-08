package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class MeterAlarmUseCasesTest extends IntegrationTest {

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private MeterAlarmUseCases meterAlarmUseCases;

  @Test
  public void testAlarmClosed() {
    ZonedDateTime start = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);
    var physicalMeter = physicalMeter().build();
    var meter = given(logicalMeter().physicalMeter(physicalMeter));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0));

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(start.minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(start
        .minusDays(4).truncatedTo(ChronoUnit.HOURS))
      .build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(0);
  }

  @Test
  public void testAlarmNotClosedForMeasurementNotOlderThanADay() {
    var now = ZonedDateTime.now();
    var start = now.minusHours(23);
    var physicalMeter = physicalMeter().build();
    var meter = given(logicalMeter().physicalMeter(physicalMeter));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0));

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(now.minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(now.minusDays(4).truncatedTo(ChronoUnit.HOURS))
      .build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(1);
  }

  @Test
  public void testAlarmNotClosed() {
    var now = ZonedDateTime.now();
    var start = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
    var physicalMeter = physicalMeter().build();
    var meter = given(logicalMeter().physicalMeter(physicalMeter));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0));

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(now.minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(now.minusDays(1).truncatedTo(ChronoUnit.HOURS)).build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(1);
  }

  @Test
  public void testAlarmNotClosedSameLastSeenAsValue() {
    var now = ZonedDateTime.now();
    var start = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
    var physicalMeter = physicalMeter().build();
    var meter = given(logicalMeter().physicalMeter(physicalMeter));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0));

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(now.minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(now.minusDays(1).truncatedTo(ChronoUnit.DAYS)).build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).toArray()).hasSize(1);
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    measurementJpaRepository.deleteAll();
  }
}
