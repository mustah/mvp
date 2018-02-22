package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UseCaseConfig {

  private final Users users;
  private final LogicalMeters logicalMeters;
  private final Settings settings;
  private final Measurements measurements;
  private final Organisations organisations;
  private final Gateways gateways;

  @Autowired
  UseCaseConfig(
    Users users,
    Settings settings,
    LogicalMeters logicalMeters,
    Measurements measurements,
    Organisations organisations,
    Gateways gateways
  ) {
    this.users = users;
    this.logicalMeters = logicalMeters;
    this.settings = settings;
    this.measurements = measurements;
    this.organisations = organisations;
    this.gateways = gateways;
  }

  @Bean
  SettingUseCases settingUseCases() {
    return new SettingUseCases(settings);
  }

  @Bean
  UserUseCases userUseCases(AuthenticatedUser currentUser) {
    return new UserUseCases(currentUser, users, new OrganisationPermissions(users));
  }

  @Bean
  LogicalMeterUseCases logicalMeterUseCases(
    AuthenticatedUser currentUser,
    Measurements measurements
  ) {
    return new LogicalMeterUseCases(currentUser, logicalMeters, measurements);
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
  GatewayUseCases gatewayUseCases() {
    return new GatewayUseCases(gateways);
  }
}
