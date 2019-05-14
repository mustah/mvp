package com.elvaco.mvp.schedule;

import com.elvaco.mvp.core.usecase.MeterAlarmUseCases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduledTasks {

  private final MeterAlarmUseCases meterAlarmUseCases;

  //Every day att 07:00 am
  @Scheduled(cron = "0 0 7 * * *")
  public void closeAlarms() {
    log.info("Closing alarms");
    meterAlarmUseCases.closeAlarms();
  }

}
