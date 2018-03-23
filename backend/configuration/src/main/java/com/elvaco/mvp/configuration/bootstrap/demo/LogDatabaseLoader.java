package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Statuses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.domainmodels.StatusType.INFO;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Order(4)
@Profile("demo")
@Component
public class LogDatabaseLoader implements CommandLineRunner {

  private final PhysicalMeters physicalMeters;
  private final Statuses statuses;
  private final MeterStatusLogs meterStatusLogs;
  private final Gateways gateways;
  private final GatewayStatusLogs gatewayStatusLogs;

  @Autowired
  public LogDatabaseLoader(
    PhysicalMeters physicalMeters,
    Statuses statuses,
    MeterStatusLogs meterStatusLogs,
    Gateways gateways,
    GatewayStatusLogs gatewayStatusLogs
  ) {
    this.physicalMeters = physicalMeters;
    this.statuses = statuses;
    this.meterStatusLogs = meterStatusLogs;
    this.gateways = gateways;
    this.gatewayStatusLogs = gatewayStatusLogs;
  }

  @Override
  @Transactional
  public void run(String... args) {
    List<Status> statuses = this.statuses.findAll();
    if (statuses.size() == 0) {
      createStatusMockData();

      createStatusLogMockData();

      createGatewayLogMockdata();
    }
  }

  private void createGatewayLogMockdata() {
    List<Gateway> gatewayList = gateways.findAll();
    List<GatewayStatusLog> gatewayStatusLogsList = new ArrayList<>();

    List<Status> statuses = this.statuses.findAll()
      .stream()
      .filter(this::onStatus)
      .collect(toList());

    for (int x = 0; x < gatewayList.size(); x++) {
      Status status = getStatus(x, statuses);

      gatewayStatusLogsList.add(new GatewayStatusLog(
        null,
        gatewayList.get(x).id,
        gatewayList.get(x).organisationId,
        status.id,
        status.name,
        ZonedDateTime.now(),
        null
      ));
    }

    gatewayStatusLogs.save(gatewayStatusLogsList);
  }

  private void createStatusMockData() {
    statuses.save(asList(
      new Status(OK.name),
      new Status(WARNING.name)
    ));
  }

  private void createStatusLogMockData() {
    List<Status> statuses = this.statuses.findAll()
      .stream()
      .filter(this::onStatus)
      .collect(toList());

    int daySeed = 10;
    int hourSeed = 0;

    List<MeterStatusLog> statusLogs = new ArrayList<>();
    for (PhysicalMeter meter : physicalMeters.findAll()) {
      daySeed++;

      Status status = getStatus(daySeed, statuses);

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

  private Status getStatus(int hourSeed, List<Status> statuses) {
    if (hourSeed % 7 == 0) {
      return statuses.get(1);
    } else {
      return statuses.get(0);
    }
  }

  private boolean onStatus(Status status) {
    return status.name.equalsIgnoreCase(INFO.name)
           || status.name.equalsIgnoreCase(OK.name)
           || status.name.equalsIgnoreCase(WARNING.name);
  }

  private ZonedDateTime addDays(int daySeed, int hourSeed) {
    return ZonedDateTime.now().plusDays(daySeed).plusHours(hourSeed);
  }
}
