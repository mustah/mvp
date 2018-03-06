package com.elvaco.mvp.configuration.bootstrap.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatuses;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Slf4j
@Order(4)
@Profile("demo")
@Component
public class LogDatabaseLoader implements CommandLineRunner {

  private final PhysicalMeters physicalMeters;
  private final MeterStatuses meterStatuses;
  private final MeterStatusLogs meterStatusLogs;

  @Autowired
  public LogDatabaseLoader(
    PhysicalMeters physicalMeters,
    MeterStatuses meterStatuses,
    MeterStatusLogs meterStatusLogs
  ) {
    this.physicalMeters = physicalMeters;
    this.meterStatuses = meterStatuses;
    this.meterStatusLogs = meterStatusLogs;
  }

  @Override
  public void run(String... args) {
    createStatusMockData();

    createStatusLogMockData();
  }

  private void createStatusMockData() {
    meterStatuses.save(asList(
      new MeterStatus("active"),
      new MeterStatus("ok"),
      new MeterStatus("warning"),
      new MeterStatus("critical"),
      new MeterStatus("info"),
      new MeterStatus("maintenance scheduled")
    ));
  }

  private void createStatusLogMockData() {
    List<MeterStatusLog> statusLogs = new ArrayList<>();

    List<MeterStatus> statuses = meterStatuses.findAll();
    List<PhysicalMeter> meters = physicalMeters.findAll();

    int daySeed = 10;
    int hourSeed = 0;

    for (PhysicalMeter meter : meters) {
      daySeed++;
      for (MeterStatus status : statuses) {
        hourSeed++;
        statusLogs.add(
          new MeterStatusLog(
            null,
            meter.id,
            status.id,
            status.name,
            addDays(daySeed, hourSeed),
            addDays(daySeed - 10, hourSeed)
          )
        );
      }
      hourSeed = 0;
    }

    meterStatusLogs.save(statusLogs);
  }

  private Date addDays(int daySeed, int hourSeed) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, -daySeed);
    calendar.add(Calendar.HOUR, hourSeed);

    return calendar.getTime();
  }
}
