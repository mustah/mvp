package com.elvaco.mvp.testdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.domainmodels.Dashboard.DashboardBuilder;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Gateway.GatewayBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter.LogicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Measurement.MeasurementBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinition.MeterDefinitionBuilder;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Organisation.OrganisationBuilder;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry.StatusLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.domainmodels.Widget.WidgetBuilder;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.testing.fixture.TestFixtures;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class IntegrationTestFixtureContext implements TestFixtures {

  public final Organisation organisation;
  public final User mvpUser;
  public final User mvpAdmin;
  public final User otcAdmin;
  public final User superAdmin;

  private final LogicalMeters logicalMeters;
  private final Gateways gateways;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final GatewayStatusLogs gatewayStatusLogs;
  private final MeterAlarmLogs meterAlarmLogs;
  private final Measurements measurements;
  private final MeterDefinitions meterDefinitions;
  private final Organisations organisations;
  private final Users users;
  private final UserSelections userSelections;
  private final Dashboards dashboards;
  private final Widgets widgets;

  @Override
  public Organisation defaultOrganisation() {
    return organisation;
  }

  Organisation given(OrganisationBuilder organisationBuilder) {
    Organisation organisation = organisationBuilder.build();

    if (organisation.selection != null) {
      userSelections.save(organisation.selection);
    }

    return organisations.saveAndFlush(organisation);
  }

  OrganisationWithUsers given(OrganisationBuilder organisationBuilder, UserBuilder... newUsers) {
    Organisation organisation = given(organisationBuilder);
    List<User> orgUsers = new ArrayList<>(newUsers.length);
    for (UserBuilder newUser : newUsers) {
      User builtUser = newUser.organisation(organisation).build();
      orgUsers.add(users.save(builtUser).withPassword(builtUser.password));
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

  LogicalMeter given(LogicalMeterBuilder logicalMeterBuilder, boolean withConnectedPhysicalMeter) {
    LogicalMeter builtMeter = logicalMeterBuilder.build();
    LogicalMeter logicalMeter = logicalMeters.save(builtMeter);
    if (builtMeter.physicalMeters.isEmpty() && withConnectedPhysicalMeter) {
      PhysicalMeter physicalMeter = physicalMeters.save(connect(
        logicalMeter,
        physicalMeter().build()
      ));
      return logicalMeter.toBuilder().physicalMeter(physicalMeter).build();
    } else if (builtMeter.physicalMeters.isEmpty()) {
      return logicalMeter;
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

  Collection<? extends AlarmLogEntry> given(AlarmLogEntryBuilder... alarmLogEntryBuilders) {
    return meterAlarmLogs.save(Stream.of(alarmLogEntryBuilders)
      .map(AlarmLogEntryBuilder::build)
      .collect(toList()));
  }

  void given(LogicalMeter logicalMeter, MeasurementBuilder... measurementBuilders) {
    Arrays.stream(measurementBuilders)
      .map(MeasurementBuilder::build)
      .forEach(measurement -> measurements.save(measurement, logicalMeter));
  }

  void given(Collection<Measurement> series, LogicalMeter logicalMeter) {
    series.forEach(m -> measurements.save(m, logicalMeter));
  }

  MeterDefinition given(MeterDefinitionBuilder meterDefinitionBuilder) {
    return meterDefinitions.save(meterDefinitionBuilder.build());
  }

  User given(UserBuilder userBuilder) {
    User user = userBuilder.build();
    return users.save(user).withPassword(user.password);
  }

  Dashboard given(DashboardBuilder dashboardBuilder) {
    return dashboards.save(dashboardBuilder.build());
  }

  Collection<Measurement> given(MeasurementSeriesBuilder seriesBuilder) {
    return seriesBuilder.build().stream()
      .map(measurement -> measurements.save(measurement, seriesBuilder.logicalMeter))
      .collect(toList());
  }

  Widget given(WidgetBuilder widgetBuilder) {
    return widgets.save(widgetBuilder.build());
  }

  private LogicalMeter given(LogicalMeterBuilder logicalMeterBuilder) {
    return given(logicalMeterBuilder, true);
  }

  private static PhysicalMeter connect(LogicalMeter logicalMeter, PhysicalMeter physicalMeter) {
    return physicalMeter.toBuilder()
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .logicalMeterId(logicalMeter.id)
      .build();
  }
}
