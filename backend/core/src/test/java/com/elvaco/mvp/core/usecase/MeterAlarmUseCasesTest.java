package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockMeterAlarmLogs;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeterAlarmUseCasesTest extends DefaultTestFixture {

  private Measurements measurements;
  private MeterAlarmLogs meterAlarmLogs;
  private LogicalMeter logicalMeter;
  private PhysicalMeter physicalMeter;
  private MeterAlarmUseCases meterAlarmUseCases;

  @Before
  public void before() {
    measurements = new MockMeasurements();
    meterAlarmLogs = new MockMeterAlarmLogs();
    physicalMeter = physicalMeter().build();
    logicalMeter = logicalMeter().physicalMeter(physicalMeter).build();
    meterAlarmUseCases = new MeterAlarmUseCases(measurements, meterAlarmLogs);
  }

  @Test
  public void testAlarmClosed() {
    measurements.save(Measurement.builder()
      .readoutTime(ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS))
      .quantity("temperature")
      .physicalMeter(physicalMeter)
      .build(),
      logicalMeter);

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(ZonedDateTime.now().minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(ZonedDateTime.now()
        .minusDays(4).truncatedTo(ChronoUnit.HOURS))
      .build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(0);
  }

  @Test
  public void testAlarmNotClosedForMeasurementNotOlderThanADay() {
    measurements.save(Measurement.builder()
      .readoutTime(ZonedDateTime.now().minusHours(23))
      .quantity("temperature")
      .physicalMeter(physicalMeter)
      .build(),
      logicalMeter);

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(ZonedDateTime.now().minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(ZonedDateTime.now()
        .minusDays(4).truncatedTo(ChronoUnit.HOURS))
      .build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(1);
  }

  @Test
  public void testAlarmNotClosed() {
    measurements.save(Measurement.builder()
      .readoutTime(ZonedDateTime.now()
        .minusDays(1)
        .truncatedTo(ChronoUnit.DAYS))
      .quantity("temperature")
      .physicalMeter(physicalMeter).build(),
      logicalMeter);

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(ZonedDateTime.now().minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.HOURS)).build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).count()).isEqualTo(1);
  }

  @Test
  public void testAlarmNotClosedSameLastSeenAsValue() {
    measurements.save(Measurement.builder()
      .readoutTime(ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS))
      .quantity("temperature")
      .physicalMeter(physicalMeter).build(),
      logicalMeter);

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(ZonedDateTime.now().minusDays(5).truncatedTo(ChronoUnit.HOURS))
      .lastSeen(ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS)).build());

    meterAlarmUseCases.closeAlarms();

    assertThat(meterAlarmLogs.findActiveAlarmsOlderThan(ZonedDateTime.now()).toArray()).hasSize(1);
  }
}
