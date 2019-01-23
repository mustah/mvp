package com.elvaco.mvp.testdata;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Gateway.GatewayBuilder;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter.LogicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Measurement.MeasurementBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry.StatusLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;

import lombok.RequiredArgsConstructor;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;
  public final User admin;
  public final User superAdmin;
  public final OrganisationEntity organisationEntity2;
  public final User user2;
  public final User admin2;
  public final User superAdmin2;
  private final LogicalMeters logicalMeters;
  private final Gateways gateways;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final GatewayStatusLogs gatewayStatusLogs;
  private final MeterAlarmLogs meterAlarmLogs;
  private final Measurements measurements;

  public Organisation organisation() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity);
  }

  public Organisation organisation2() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity2);
  }

  public UUID organisationId() {
    return organisation().id;
  }

  public UUID organisationId2() {
    return organisation2().id;
  }

  public ZonedDateTime now() {
    return
      // some tests want to test the missing_measurements view, which is only constructed for a
      // maximum of three months back, so we need to make "context now" relative to "actual now"
      ZonedDateTime.now()
        .truncatedTo(ChronoUnit.DAYS)
        .withDayOfMonth(1)
        // some tests still build URL:s as strings - can't have + signs in those without triggerin
        // RestClientExceptions
        .withZoneSameLocal(ZoneId.of("Z"));
  }

  public ZonedDateTime yesterday() {
    return now().minusDays(1);
  }

  GatewayBuilder gateway() {
    UUID gatewayId = randomUUID();
    return Gateway.builder()
      .organisationId(organisationId())
      .id(gatewayId)
      .serial(gatewayId.toString())
      .productModel(randomUUID().toString());
  }

  Collection<Measurement> series(
    PhysicalMeter physicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    TemporalAmount interval,
    double... values
  ) {
    List<Measurement> series = new ArrayList<>();
    ZonedDateTime t = start;
    for (double value : values) {
      series.add(
        measurement(physicalMeter, quantity)
          .value(value)
          .created(t)
          .quantity(quantity.name)
          .unit(quantity.storageUnit)
          .build()
      );

      t = t.plus(interval);
    }
    return series;
  }

  Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    TemporalAmount interval,
    double... values
  ) {
    List<Measurement> series = new ArrayList<>();
    ZonedDateTime t = start;
    for (double value : values) {
      series.add(
        measurement(logicalMeter)
          .value(value)
          .created(t)
          .quantity(quantity.name)
          .unit(quantity.storageUnit)
          .build()
      );

      t = t.plus(interval);
    }
    return series;
  }

  Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    double... values
  ) {
    return series(
      logicalMeter,
      quantity,
      start,
      Duration.ofMinutes(logicalMeter.activePhysicalMeter(now()).orElseThrow().readIntervalMinutes),
      values
    );
  }

  Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    TemporalAmount interval,
    double... values
  ) {
    return series(
      logicalMeter,
      quantity,
      now(),
      interval,
      values
    );
  }

  Collection<Measurement> series(LogicalMeter logicalMeter, Quantity quantity, double... values) {
    return series(
      logicalMeter,
      quantity,
      Duration.ofMinutes(logicalMeter.activePhysicalMeter(now()).orElseThrow().readIntervalMinutes),
      values
    );
  }

  MeasurementBuilder measurement(LogicalMeter logicalMeter) {
    Quantity quantity = logicalMeter.getQuantities().iterator().next();
    return Measurement.builder()
      .physicalMeter(logicalMeter.activePhysicalMeter(now()).orElseThrow())
      .unit(quantity.presentationUnit())
      .value(0.0)
      .quantity(quantity.name)
      .created(now());
  }

  MeasurementBuilder measurement(PhysicalMeter physicalMeter, Quantity quantity) {
    return Measurement.builder()
      .physicalMeter(physicalMeter)
      .unit(quantity.presentationUnit())
      .value(0.0)
      .quantity(quantity.name)
      .created(now());
  }

  LogicalMeterBuilder logicalMeter() {
    UUID logicalMeterId = randomUUID();
    return LogicalMeter.builder()
      .organisationId(organisationId())
      .id(logicalMeterId)
      .created(now().minusYears(1))
      .externalId(logicalMeterId.toString())
      .utcOffset(
        String.format("%+03d", Duration.ofSeconds(now().getOffset().getTotalSeconds()).toHours())
      )
      .meterDefinition(MeterDefinition.DISTRICT_HEATING_METER)
      .location(Location.UNKNOWN_LOCATION);
  }

  PhysicalMeterBuilder physicalMeter() {
    UUID physicalMeterId = randomUUID();
    return PhysicalMeter.builder()
      .id(physicalMeterId)
      .externalId(physicalMeterId.toString())
      .address(physicalMeterId.toString())
      .readIntervalMinutes(60)
      .organisationId(organisationId())
      .activePeriod(PeriodRange.closedFrom(null, null));
  }

  StatusLogEntryBuilder statusLog(LogicalMeter logicalMeter) {
    return StatusLogEntry.builder()
      .primaryKey(
        logicalMeter.activePhysicalMeter(now()).orElseThrow().primaryKey()
      );
  }

  StatusLogEntryBuilder statusLog(Gateway gateway) {
    return StatusLogEntry.builder().primaryKey(gateway.primaryKey());
  }

  AlarmLogEntryBuilder alarm(LogicalMeter logicalMeter) {
    return AlarmLogEntry.builder()
      .primaryKey(logicalMeter.activePhysicalMeter(now()).orElseThrow().primaryKey())
      .start(now())
      .mask(0);
  }

  LogicalMeter given(PhysicalMeterBuilder physicalMeterBuilder) {
    LogicalMeter logicalMeter = logicalMeters.save(logicalMeter().build());
    return logicalMeter.addPhysicalMeter(physicalMeters.save(
      physicalMeterBuilder.logicalMeterId(logicalMeter.id)
        .externalId(logicalMeter.externalId)
        .build()
    ));
  }

  LogicalMeter given(LogicalMeterBuilder logicalMeterBuilder) {
    LogicalMeter logicalMeter = logicalMeters.save(logicalMeterBuilder.build());
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .organisationId(logicalMeter.organisationId)
        .logicalMeterId(logicalMeter.id)
        .externalId(logicalMeter.externalId)
        .build()
    );

    return logicalMeter.addPhysicalMeter(physicalMeter);
  }

  Collection<LogicalMeter> given(LogicalMeterBuilder... logicalMeterBuilders) {
    return Arrays.stream(logicalMeterBuilders).map(this::given).collect(Collectors.toList());
  }

  LogicalMeter given(
    LogicalMeterBuilder logicalMeterBuilder,
    PhysicalMeterBuilder... physicalMeterBuilders
  ) {
    final LogicalMeter logicalMeter = logicalMeters.save(logicalMeterBuilder.build());

    var builtPhysicalMeters = Arrays.stream(physicalMeterBuilders)
      .map(pm -> pm
        .logicalMeterId(logicalMeter.id)
        .externalId(logicalMeter.externalId)
        .build()
      )
      .map(physicalMeters::save)
      .collect(toList());

    LogicalMeter withPhysicalMeters = logicalMeter;

    for (PhysicalMeter meter : builtPhysicalMeters) {
      withPhysicalMeters = withPhysicalMeters.addPhysicalMeter(meter);
    }

    return withPhysicalMeters;
  }

  Gateway given(GatewayBuilder gateway) {
    var builtGateway = gateway.build();

    List<LogicalMeter> savedMeters = new ArrayList<>();
    builtGateway.meters
      .forEach(meter -> {
        List<Gateway> gateways = meter.gateways.stream()
          .filter(g -> !g.id.equals(builtGateway.id))
          .collect(toList());
        gateways.add(builtGateway);

        savedMeters.add(logicalMeters.save(meter.toBuilder().gateways(gateways).build()));
      });

    return gateways.save(builtGateway.toBuilder().meters(savedMeters).build());
  }

  Collection<Gateway> given(GatewayBuilder... gatewayBuilders) {
    return Arrays.stream(gatewayBuilders).map(this::given).collect(toList());
  }

  void given(StatusLogEntryBuilder... statusLogEntryBuilders) {
    meterStatusLogs.save(Arrays.stream(statusLogEntryBuilders)
      .map(StatusLogEntryBuilder::build)
      .collect(toList()));
  }

  void given(GatewayStatusLogEntryBuilderDelegate... gatewayStatusLogEntryBuilderDelegates) {
    gatewayStatusLogs.save(Arrays.stream(gatewayStatusLogEntryBuilderDelegates)
      .map(GatewayStatusLogEntryBuilderDelegate::build)
      .collect(toList()));
  }

  Collection<? extends AlarmLogEntry> given(
    AlarmLogEntryBuilder... alarmLogEntryBuilders
  ) {
    return meterAlarmLogs.save(Arrays.stream(alarmLogEntryBuilders)
      .map(AlarmLogEntryBuilder::build)
      .collect(toList()));
  }

  void given(MeasurementBuilder... measurementBuilders) {
    Arrays.stream(measurementBuilders)
      .map(MeasurementBuilder::build)
      .forEach(measurements::save);
  }

  void given(Collection<Measurement> series) {
    series.forEach(measurements::save);
  }
}
