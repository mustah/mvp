package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.MissingMeasurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LocationUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MapUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.MeterAlarmUseCases;
import com.elvaco.mvp.core.usecase.MissingMeasurementUseCases;
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
  private final Measurements measurements;
  private final Organisations organisations;
  private final Gateways gateways;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final TokenService tokenService;
  private final UserSelections userSelections;
  private final Locations locations;
  private final Properties properties;
  private final MeterAlarmLogs meterAlarmLogs;

  @Bean
  SettingUseCases settingUseCases() {
    return new SettingUseCases(settings);
  }

  @Bean
  UserUseCases userUseCases(AuthenticatedUser currentUser) {
    return new UserUseCases(currentUser, users, new OrganisationPermissions(users), tokenService);
  }

  @Bean
  LogicalMeterUseCases logicalMeterUseCases(AuthenticatedUser currentUser) {
    return new LogicalMeterUseCases(currentUser, logicalMeters);
  }

  @Bean
  OrganisationUseCases organisationUseCases(AuthenticatedUser currentUser) {
    return new OrganisationUseCases(currentUser, organisations, new OrganisationPermissions(users));
  }

  @Bean
  MeasurementUseCases measurementUseCases(AuthenticatedUser currentUser) {
    return new MeasurementUseCases(currentUser, measurements);
  }

  @Bean
  GatewayUseCases gatewayUseCases(AuthenticatedUser currentUser) {
    return new GatewayUseCases(gateways, currentUser);
  }

  @Bean
  PhysicalMeterUseCases physicalMeterUseCases(AuthenticatedUser currentUser) {
    return new PhysicalMeterUseCases(currentUser, physicalMeters, meterStatusLogs);
  }

  @Bean
  DashboardUseCases dashboardUseCases(AuthenticatedUser currentUser) {
    return new DashboardUseCases(logicalMeters, currentUser);
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
  MissingMeasurementUseCases missingMeasurementUseCases(
    AuthenticatedUser currentUser,
    MissingMeasurements missingMeasurements
  ) {
    return new MissingMeasurementUseCases(currentUser, missingMeasurements);
  }

  @Bean
  MeterAlarmUseCases meterAlarmUseCases() {
    return new MeterAlarmUseCases(measurements, meterAlarmLogs);
  }
}
