package com.elvaco.mvp.testing.fixture;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.domainmodels.Dashboard.DashboardBuilder;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Gateway.GatewayBuilder;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter.LogicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Measurement.MeasurementBuilder;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Medium.MediumBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinition.MeterDefinitionBuilder;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Organisation.OrganisationBuilder;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry.StatusLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.domainmodels.Widget.WidgetBuilder;
import com.elvaco.mvp.core.domainmodels.WidgetType;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.Slugify;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;

public interface TestFixtures {
  Organisation defaultOrganisation();

  default Random random() {
    return new Random();
  }

  default UUID organisationId() {
    return defaultOrganisation().id;
  }

  default ZonedDateTime now() {
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

  default ZonedDateTime yesterday() {
    return now().minusDays(1);
  }

  default OrganisationBuilder organisation() {
    UUID organisationId = randomUUID();
    return Organisation.builder()
      .id(organisationId)
      .slug(Slugify.slugify(organisationId.toString()))
      .externalId(organisationId.toString())
      .name(organisationId.toString());
  }

  default OrganisationBuilder subOrganisation() {
    UUID organisationId = randomUUID();

    try {
      UserSelection userSelection = UserSelection.builder()
        .id(randomUUID())
        .selectionParameters(OBJECT_MAPPER.readTree("{\"test\": \"json\"}"))
        .organisationId(organisationId)
        .build();

      return Organisation.builder()
        .id(organisationId)
        .slug(Slugify.slugify(organisationId.toString()))
        .externalId(organisationId.toString())
        .name(organisationId.toString())
        .parent(organisation().build())
        .selection(userSelection);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  default UserBuilder newUser() {
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

  default GatewayBuilder gateway() {
    UUID gatewayId = randomUUID();
    return Gateway.builder()
      .organisationId(organisationId())
      .id(gatewayId)
      .serial(gatewayId.toString())
      .productModel(randomUUID().toString());
  }

  default MeterDefinitionBuilder meterDefinition() {
    return MeterDefinition.builder()
      .id(random().nextLong())
      .name(randomUUID().toString())
      .organisation(defaultOrganisation())
      .medium(medium().build());
  }

  default MediumBuilder medium() {
    return Medium.builder()
      .id(random().nextLong())
      .name(randomUUID().toString());
  }

  default Collection<Measurement> series(
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

  default Collection<Measurement> series(
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

  default Collection<Measurement> series(
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

  default Collection<Measurement> series(
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

  default Collection<Measurement> series(
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

  default Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    double... values
  ) {
    return series(
      logicalMeter,
      quantity,
      Duration.ofMinutes(logicalMeter.activePhysicalMeter(now()).orElseThrow().readIntervalMinutes),
      values
    );
  }

  default MeasurementBuilder measurement(LogicalMeter logicalMeter) {
    DisplayQuantity quantity = logicalMeter.getQuantities().iterator().next();
    return Measurement.builder()
      .physicalMeter(logicalMeter.activePhysicalMeter(now()).orElseThrow())
      .unit(quantity.quantity.storageUnit)
      .value(0.0)
      .quantity(quantity.quantity.name)
      .created(now());
  }

  default MeasurementBuilder measurement(
    PhysicalMeter physicalMeter,
    Quantity quantity
  ) {
    return Measurement.builder()
      .physicalMeter(physicalMeter)
      .unit(quantity.storageUnit)
      .value(0.0)
      .quantity(quantity.name)
      .created(now());
  }

  default LogicalMeterBuilder logicalMeter() {
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

  default PhysicalMeterBuilder physicalMeter() {
    UUID physicalMeterId = randomUUID();
    return PhysicalMeter.builder()
      .id(physicalMeterId)
      .externalId(physicalMeterId.toString())
      .address(String.valueOf(random().nextInt(999999)))
      .readIntervalMinutes(60)
      .organisationId(organisationId())
      .activePeriod(PeriodRange.closedFrom(null, null));
  }

  default StatusLogEntryBuilder statusLog(LogicalMeter logicalMeter) {
    return StatusLogEntry.builder()
      .primaryKey(
        logicalMeter.activePhysicalMeter(now()).orElseThrow().primaryKey()
      );
  }

  default StatusLogEntryBuilder statusLog(Gateway gateway) {
    return StatusLogEntry.builder().primaryKey(gateway.primaryKey());
  }

  default AlarmLogEntryBuilder alarm(LogicalMeter logicalMeter) {
    return AlarmLogEntry.builder()
      .primaryKey(logicalMeter.activePhysicalMeter(now()).orElseThrow().primaryKey())
      .start(now())
      .mask(0);
  }

  default DashboardBuilder dashboard() {
    return Dashboard.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(organisationId())
      .name(randomUUID().toString())
      .layout(OBJECT_MAPPER.createObjectNode());
  }

  default WidgetBuilder widget() {
    return Widget.builder()
      .id(randomUUID())
      .dashboardId(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(organisationId())
      .type(WidgetType.COLLECTION)
      .title(randomUUID().toString())
      .settings(OBJECT_MAPPER.createObjectNode());
  }

  default UnitConverter unitConverter(boolean isSameDimension) {
    return new UnitConverter() {
      @Override
      public MeasurementUnit convert(
        MeasurementUnit measurementUnit, String targetUnit
      ) {
        return measurementUnit;
      }

      @Override
      public boolean isSameDimension(String firstUnit, String secondUnit) {
        return isSameDimension;
      }
    };
  }
}
