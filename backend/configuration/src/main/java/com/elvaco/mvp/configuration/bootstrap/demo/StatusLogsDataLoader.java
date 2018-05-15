package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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

  void loadMockData() {
    createMeterStatusLogMockData();
    createGatewayLogMockData();
  }

  private void createGatewayLogMockData() {
    gateways.findAll(new RequestParametersAdapter())
      .stream()
      .map(gateway -> new StatusLogEntry<>(
        gateway.id,
        nextRandomStatusType(),
        subtractDays(90)
      ))
      .forEach(gatewayStatusLogs::save);
  }

  private void createMeterStatusLogMockData() {
    int daySeed = 1;

    List<StatusLogEntry<UUID>> statusLogs = new ArrayList<>();
    for (PhysicalMeter meter : physicalMeters.findAll()) {
      daySeed++;
      statusLogs.add(
        new StatusLogEntry<>(
          meter.id,
          ACTIVE,
          subtractDays(daySeed)
        )
      );

      statusLogs.add(
        new StatusLogEntry<>(
          meter.id,
          nextRandomStatusType(),
          subtractDays(daySeed)
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
