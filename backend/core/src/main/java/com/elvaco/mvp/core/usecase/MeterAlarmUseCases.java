package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterAlarmUseCases {

  private final Measurements measurements;
  private final MeterAlarmLogs meterAlarmLogs;

  public void closeAlarms() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime oneDayAgo = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
    ZonedDateTime twoDaysAgo = now.minusDays(2).truncatedTo(ChronoUnit.DAYS);
    meterAlarmLogs.findActiveAlarmsOlderThan(twoDaysAgo)
      .forEach(alarm -> measurements.firstForPhysicalMeterWithinDateRange(
        alarm.primaryKey.getId(),
        alarm.lastSeen,
        oneDayAgo
      ).ifPresent(firstMeasurementAfterLastSeen ->
        meterAlarmLogs.save(AlarmLogEntry.builder()
          .id(alarm.id)
          .primaryKey(alarm.primaryKey)
          .mask(alarm.mask)
          .description(alarm.description)
          .start(alarm.start)
          .lastSeen(alarm.lastSeen)
          .stop(firstMeasurementAfterLastSeen.readoutTime)
          .build())));
  }
}
