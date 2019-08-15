package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.UserPermissions;
import com.elvaco.mvp.core.spi.repository.CollectionStats;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.GatewaysMeters;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.CollectionStatsUseCases;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LocationUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MapUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.MeterAlarmUseCases;
import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.core.usecase.OrganisationThemeUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserSelectionUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class UseCaseConfig {

  private final Users users;
  private final Settings settings;
  private final LogicalMeters logicalMeters;
  private final CollectionStats collectionStats;
  private final Measurements measurements;
  private final Organisations organisations;
  private final Gateways gateways;
  private final GatewaysMeters gatewaysMeters;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final TokenService tokenService;
  private final UserSelections userSelections;
  private final Dashboards dashboards;
  private final Widgets widgets;
  private final Locations locations;
  private final Properties properties;
  private final MeterAlarmLogs meterAlarmLogs;
  private final MeterDefinitions meterDefinitions;
  private final MediumProvider mediumProvider;
  private final QuantityProvider quantityProvider;
  private final SystemMeterDefinitionProvider systemMeterDefinitionProvider;

  @Bean
  SettingUseCases settingUseCases() {
    return new SettingUseCases(settings);
  }

  @Bean
  UserUseCases userUseCases(AuthenticatedUser currentUser) {
    return new UserUseCases(
      currentUser,
      users,
      new UserPermissions(users),
      tokenService,
      organisations
    );
  }

  @Bean
  LogicalMeterUseCases logicalMeterUseCases(AuthenticatedUser currentUser) {
    return new LogicalMeterUseCases(currentUser, logicalMeters);
  }

  @Bean
  CollectionStatsUseCases collectionStatsUseCases(AuthenticatedUser currentUser) {
    return new CollectionStatsUseCases(currentUser, collectionStats);
  }

  @Bean
  OrganisationUseCases organisationUseCases(AuthenticatedUser currentUser) {
    return new OrganisationUseCases(
      currentUser,
      organisations,
      new OrganisationPermissions()
    );
  }

  @Bean
  OrganisationThemeUseCases organisationThemeUseCases(
    OrganisationAssets organisationAssets,
    OrganisationThemes organisationTheme
  ) {
    return new OrganisationThemeUseCases(organisationAssets, organisationTheme);
  }

  @Bean
  MeasurementUseCases measurementUseCases(AuthenticatedUser currentUser) {
    return new MeasurementUseCases(currentUser, measurements, logicalMeters);
  }

  @Bean
  GatewayUseCases gatewayUseCases(AuthenticatedUser currentUser) {
    return new GatewayUseCases(gateways, gatewaysMeters, currentUser);
  }

  @Bean
  PhysicalMeterUseCases physicalMeterUseCases(AuthenticatedUser currentUser) {
    return new PhysicalMeterUseCases(currentUser, physicalMeters, meterStatusLogs);
  }

  @Bean
  DashboardUseCases dashboardUseCases(AuthenticatedUser currentUser) {
    return new DashboardUseCases(currentUser, dashboards, widgets);
  }

  @Bean
  UserSelectionUseCases selectionUseCases(AuthenticatedUser currentUser) {
    return new UserSelectionUseCases(currentUser, userSelections);
  }

  @Bean
  MapUseCases mapUseCases(AuthenticatedUser currentUser) {
    return new MapUseCases(currentUser, locations);
  }

  @Bean
  LocationUseCases locationUseCases(AuthenticatedUser currentUser) {
    return new LocationUseCases(currentUser, locations);
  }

  @Bean
  PropertiesUseCases propertiesUseCases(AuthenticatedUser currentUser) {
    return new PropertiesUseCases(currentUser, properties);
  }

  @Bean
  MeterAlarmUseCases meterAlarmUseCases() {
    return new MeterAlarmUseCases(meterAlarmLogs);
  }

  @Bean
  MeterDefinitionUseCases meterDefinitionUseCases(
    AuthenticatedUser currentUser,
    UnitConverter unitConverter
  ) {
    return new MeterDefinitionUseCases(
      currentUser,
      meterDefinitions,
      unitConverter,
      organisations,
      quantityProvider,
      mediumProvider,
      systemMeterDefinitionProvider,
      logicalMeters
    );
  }
}
