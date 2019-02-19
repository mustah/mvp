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
import java.util.Random;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Gateway.GatewayBuilder;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter.LogicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Measurement.MeasurementBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Organisation.OrganisationBuilder;
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
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.util.Slugify;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import lombok.RequiredArgsConstructor;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;
  public final User admin;
  public final User superAdmin;
  private final LogicalMeters logicalMeters;
  private final Gateways gateways;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final GatewayStatusLogs gatewayStatusLogs;
  private final MeterAlarmLogs meterAlarmLogs;
  private final Measurements measurements;
  private final Organisations organisations;
  private final Users users;

  private final Random random = new Random();

  public Organisation defaultOrganisation() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity);
  }

  public UUID organisationId() {
    return defaultOrganisation().id;
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

  OrganisationBuilder organisation() {
    UUID organisationId = randomUUID();
    return Organisation.builder()
      .id(organisationId)
      .slug(Slugify.slugify(organisationId.toString()))
      .externalId(organisationId.toString())
      .name(organisationId.toString());
  }

  UserBuilder newUser() {
    UUID userId = randomUUID();
    return new UserBuilder()
      .id(userId)
      .asUser()
      .language(Language.en)
      .name(userId.toString())
      .password(userId.toString())
      .email(userId.toString() + "@test.test")
      .organisation(defaultOrganisation());
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
    PhysicalMeter physicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    double... values
  ) {
    return series(
      physicalMeter,
      quantity,
      start,
      Duration.ofMinutes(physicalMeter.readIntervalMinutes),
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
    DisplayQuantity quantity = logicalMeter.getQuantities().iterator().next();
    return Measurement.builder()
      .physicalMeter(logicalMeter.activePhysicalMeter(now()).orElseThrow())
      .unit(quantity.quantity.storageUnit)
      .value(0.0)
      .quantity(quantity.quantity.name)
      .created(now());
  }

  MeasurementBuilder measurement(PhysicalMeter physicalMeter, Quantity quantity) {
    return Measurement.builder()
      .physicalMeter(physicalMeter)
      .unit(quantity.storageUnit)
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
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
      .location(Location.UNKNOWN_LOCATION);
  }

  PhysicalMeterBuilder physicalMeter() {
    UUID physicalMeterId = randomUUID();
    return PhysicalMeter.builder()
      .id(physicalMeterId)
      .externalId(physicalMeterId.toString())
      .address(String.valueOf(random.nextInt(999999)))
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

  Organisation given(OrganisationBuilder organisationBuilder) {
    return organisations.save(organisationBuilder.build());
  }

  OrganisationWithUsers given(OrganisationBuilder organisationBuilder, UserBuilder... newUsers) {
    Organisation organisation = given(organisationBuilder);
    List<User> orgUsers = new ArrayList<>(newUsers.length);
    for (UserBuilder newUser : newUsers) {
      User builtUser = newUser.organisation(organisation).build();
      orgUsers.add(
        users.save(builtUser)
          .withPassword(builtUser.password)
      );
    }
    return new OrganisationWithUsers(organisation, orgUsers);
  }

  Collection<Organisation> given(OrganisationBuilder... organisationBuilders) {
    return Arrays.stream(organisationBuilders).map(this::given).collect(toList());
  }

  LogicalMeter given(PhysicalMeterBuilder physicalMeterBuilder) {
    var logicalMeter = logicalMeters.save(logicalMeter().build());
    var physicalMeter = physicalMeters.save(connect(logicalMeter, physicalMeterBuilder.build()));
    return logicalMeter.toBuilder().physicalMeter(physicalMeter).build();
  }

  LogicalMeter given(LogicalMeterBuilder logicalMeterBuilder) {
    LogicalMeter builtMeter = logicalMeterBuilder.build();
    LogicalMeter logicalMeter = logicalMeters.save(builtMeter);
    if (builtMeter.physicalMeters.isEmpty()) {
      PhysicalMeter physicalMeter = physicalMeters.save(connect(
        logicalMeter,
        physicalMeter().build()
      ));
      return logicalMeter.toBuilder().physicalMeter(physicalMeter).build();
    } else {
      return logicalMeter.toBuilder()
        .physicalMeters(builtMeter.physicalMeters.stream()
          .map(physicalMeter -> connect(logicalMeter, physicalMeter))
          .map(physicalMeters::save)
          .collect(toList()))
        .build();
    }
  }

  Collection<LogicalMeter> given(LogicalMeterBuilder... logicalMeterBuilders) {
    return Arrays.stream(logicalMeterBuilders).map(this::given).collect(toList());
  }

  LogicalMeter given(
    LogicalMeterBuilder logicalMeterBuilder,
    PhysicalMeterBuilder... physicalMeterBuilders
  ) {
    final LogicalMeter logicalMeter = logicalMeters.save(logicalMeterBuilder.build());

    var builtPhysicalMeters = Arrays.stream(physicalMeterBuilders)
      .map(pm -> connect(logicalMeter, pm.build()))
      .map(physicalMeters::save)
      .collect(toList());

    return logicalMeter.toBuilder().physicalMeters(builtPhysicalMeters).build();
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

  private static PhysicalMeter connect(LogicalMeter logicalMeter, PhysicalMeter physicalMeter) {
    return physicalMeter.toBuilder()
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .logicalMeterId(logicalMeter.id)
      .build();
  }
}
