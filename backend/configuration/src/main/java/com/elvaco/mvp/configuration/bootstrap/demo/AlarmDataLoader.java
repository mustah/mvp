package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.elvaco.mvp.core.spi.repository.AlarmDescriptions;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("demo")
@Component
public class AlarmDataLoader implements MockDataLoader {
  private final MeterAlarmLogs meterAlarmLogs;
  private final PhysicalMeters physicalMeters;
  private final AlarmDescriptions alarmDescriptions;

  public void load(Random random) {
    physicalMeters.findAll()
      .stream()
      .filter((ignore -> random.nextBoolean()))
      .forEach((meter) -> {
        var availableMasks = alarmDescriptions.descriptionsFor(
          meter.manufacturer,
          meter.mbusDeviceType,
          meter.revision
        ).keySet();
        if (availableMasks.isEmpty()) {
          return;
        }

        availableMasks
          .stream()
          .filter((mask) -> random.nextBoolean())
          .forEach(mask ->
            meterAlarmLogs.createOrUpdate(
              meter.primaryKey(),
              mask,
              randomStartDate(random)
            )
          );
      });
  }

  private ZonedDateTime randomStartDate(Random random) {
    return ZonedDateTime.now()
      .minusDays(random.nextInt(90))
      .plusHours(random.nextInt(24))
      .truncatedTo(ChronoUnit.HOURS);
  }
}
