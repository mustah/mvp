package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;

@Profile("demo")
@Component
@RequiredArgsConstructor
class StatusLogsDataLoader implements MockDataLoader {

  private static final StatusType[] STATUS_TYPES = {OK, ERROR};

  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final Gateways gateways;
  private final GatewayStatusLogs gatewayStatusLogs;

  public void load(Random random) {
    createMeterStatusLogMockData(random);
    createGatewayLogMockData(random);
  }

  private void createGatewayLogMockData(Random random) {
    gateways.findAll().stream()
      .map(gateway -> StatusLogEntry.builder()
        .primaryKey(gateway.primaryKey())
        .status(nextRandomStatusType(random))
        .start(subtractDays(90))
        .build())
      .forEach(gatewayStatusLogs::save);
  }

  private void createMeterStatusLogMockData(Random random) {
    int daySeed = 1;

    List<StatusLogEntry> statusLogs = new ArrayList<>();
    for (PhysicalMeter meter : physicalMeters.findAll()) {
      daySeed++;
      statusLogs.add(StatusLogEntry.builder()
        .primaryKey(meter.primaryKey())
        .status(OK)
        .start(subtractDays(daySeed))
        .build()
      );
      if (random.nextBoolean()) {
        statusLogs.add(StatusLogEntry.builder()
          .primaryKey(meter.primaryKey())
          .status(ERROR)
          .start(subtractDays(daySeed).plusHours(1))
          .build()
        );
      }
    }
    meterStatusLogs.save(statusLogs);
  }

  private StatusType nextRandomStatusType(Random random) {
    return STATUS_TYPES[random.nextInt(STATUS_TYPES.length - 1)];
  }

  private static ZonedDateTime subtractDays(int daySeed) {
    return ZonedDateTime.now().minusDays(daySeed).plusHours(0);
  }
}
