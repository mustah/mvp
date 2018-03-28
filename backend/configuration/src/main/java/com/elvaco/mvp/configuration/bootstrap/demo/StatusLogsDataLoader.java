package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.domainmodels.StatusType.ACTIVE;
import static com.elvaco.mvp.core.domainmodels.StatusType.CRITICAL;
import static com.elvaco.mvp.core.domainmodels.StatusType.INFO;
import static com.elvaco.mvp.core.domainmodels.StatusType.MAINTENANCE_SCHEDULED;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.UNKNOWN;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;

@Profile("demo")
@Component
class StatusLogsDataLoader {

  private static final Random RANDOM = new Random();
  private static final StatusType[] STATUS_TYPES = {
    OK,
    INFO,
    CRITICAL,
    WARNING,
    UNKNOWN,
    MAINTENANCE_SCHEDULED
  };

  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final Gateways gateways;
  private final GatewayStatusLogs gatewayStatusLogs;

  @Autowired
  StatusLogsDataLoader(
    PhysicalMeters physicalMeters,
    MeterStatusLogs meterStatusLogs,
    Gateways gateways,
    GatewayStatusLogs gatewayStatusLogs
  ) {
    this.physicalMeters = physicalMeters;
    this.meterStatusLogs = meterStatusLogs;
    this.gateways = gateways;
    this.gatewayStatusLogs = gatewayStatusLogs;
  }

  void loadMockData() {
    createMeterStatusLogMockData();
    createGatewayLogMockData();
  }

  private void createGatewayLogMockData() {
    gateways.findAll(new RequestParametersAdapter())
      .stream()
      .map(gateway -> new GatewayStatusLog(
        null,
        gateway.id,
        nextRandomStatusType(),
        ZonedDateTime.now(),
        null
      ))
      .forEach(gatewayStatusLogs::save);
  }

  private void createMeterStatusLogMockData() {
    int daySeed = 1;

    List<MeterStatusLog> statusLogs = new ArrayList<>();
    for (PhysicalMeter meter : physicalMeters.findAll()) {
      daySeed++;
      statusLogs.add(
        new MeterStatusLog(
          null,
          meter.id,
          ACTIVE,
          subtractDays(daySeed),
          null
        )
      );

      statusLogs.add(
        new MeterStatusLog(
          null,
          meter.id,
          nextRandomStatusType(),
          subtractDays(daySeed),
          null
        )
      );
    }
    meterStatusLogs.save(statusLogs);
  }

  private StatusType nextRandomStatusType() {
    return STATUS_TYPES[RANDOM.nextInt(STATUS_TYPES.length - 1)];
  }

  private ZonedDateTime subtractDays(int daySeed) {
    return ZonedDateTime.now().minusDays(daySeed).plusHours(0);
  }
}
