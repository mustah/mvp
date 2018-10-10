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

    ZonedDateTime oneDayAgo = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);
    ZonedDateTime twoDaysAgo = ZonedDateTime.now().minusDays(2).truncatedTo(ChronoUnit.DAYS);
    meterAlarmLogs.findActiveAlamsOlderThan(twoDaysAgo).forEach((alarm) -> {
      measurements.firstForPhysicalMeterWithinDateRange(alarm.entityId, alarm.lastSeen,oneDayAgo)
        .ifPresent((firstMeasurementAfterLastSeen) -> {
          meterAlarmLogs.save(AlarmLogEntry.builder()
            .entityId(alarm.entityId)
            .id(alarm.id)
            .mask(alarm.mask)
            .description(alarm.description)
            .start(alarm.start)
            .lastSeen(alarm.lastSeen)
            .stop(firstMeasurementAfterLastSeen.created)
            .build());
        });
    });
  }
}
