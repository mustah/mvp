package com.elvaco.mvp.configuration.bootstrap.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
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
      new MeterStatus("Active"),
      new MeterStatus("Maintenance scheduled")
    ));
  }

  private void createStatusLogMockData() {
    List<MeterStatusLog> statuses = new ArrayList<>();

    meterStatuses.findAll().forEach(
      meterStatus -> physicalMeters.findAll().forEach(
        physicalMeter ->
          statuses.add(
            new MeterStatusLog(physicalMeter.id, meterStatus.id, meterStatus.name, new Date())
          )
      )
    );

    meterStatusLogs.save(statuses);
  }
}
