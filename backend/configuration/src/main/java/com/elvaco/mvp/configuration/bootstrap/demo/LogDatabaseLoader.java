package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import static java.util.stream.Collectors.toList;

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
    List<MeterStatus> statuses = meterStatuses.findAll();
    if (statuses.size() == 0) {
      createStatusMockData();

      createStatusLogMockData();
    }
  }

  private void createStatusMockData() {
    meterStatuses.save(asList(
      new MeterStatus("ok"),
      new MeterStatus("warning")
    ));
  }

  private void createStatusLogMockData() {
    List<MeterStatus> statuses = meterStatuses.findAll()
      .stream()
      .filter(this::onStatus)
      .collect(toList());

    int daySeed = 10;
    int hourSeed = 0;

    List<MeterStatusLog> statusLogs = new ArrayList<>();
    for (PhysicalMeter meter : physicalMeters.findAll()) {
      daySeed++;

      MeterStatus status = getStatus(daySeed, statuses);

      statusLogs.add(
        new MeterStatusLog(
          null,
          meter.id,
          status.id,
          status.name,
          addDays(daySeed, hourSeed),
          null
        )
      );
      hourSeed = 0;
    }
    meterStatusLogs.save(statusLogs);
  }

  private MeterStatus getStatus(int hourSeed, List<MeterStatus> statuses) {
    if (hourSeed % 7 == 0) {
      return statuses.get(1);
    } else {
      return statuses.get(0);
    }
  }

  private boolean onStatus(MeterStatus meterStatus) {
    return meterStatus.name.equalsIgnoreCase("info")
           || meterStatus.name.equalsIgnoreCase("ok")
           || meterStatus.name.equalsIgnoreCase("warning");
  }

  private ZonedDateTime addDays(int daySeed, int hourSeed) {
    return ZonedDateTime.now().plusDays(daySeed).plusHours(hourSeed);
  }
}
