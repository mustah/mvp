package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.MeterLocationUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserSelectionUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
class UseCaseConfig {

  private final Users users;
  private final Settings settings;
  private final LogicalMeters logicalMeters;
  private final Measurements measurements;
  private final Organisations organisations;
  private final Gateways gateways;
  private final PhysicalMeters physicalMeters;
  private final TokenService tokenService;
  private final UserSelections userSelections;

  @Bean
  SettingUseCases settingUseCases() {
    return new SettingUseCases(settings);
  }

  @Bean
  UserUseCases userUseCases(AuthenticatedUser currentUser) {
    return new UserUseCases(currentUser, users, new OrganisationPermissions(users), tokenService);
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
  GatewayUseCases gatewayUseCases(AuthenticatedUser currentUser) {
    return new GatewayUseCases(gateways, currentUser);
  }

  @Bean
  PhysicalMeterUseCases physicalMeterUseCases(AuthenticatedUser currentUser) {
    return new PhysicalMeterUseCases(currentUser, physicalMeters);
  }

  @Bean
  MeterLocationUseCases meterLocationUseCases(AuthenticatedUser currentUser) {
    return new MeterLocationUseCases(logicalMeters, currentUser);
  }

  @Bean
  DashboardUseCases dashboardUseCases(AuthenticatedUser currentUser) {
    return new DashboardUseCases(currentUser, logicalMeters);
  }

  @Bean
  UserSelectionUseCases selectionUseCases(AuthenticatedUser currentUser) {
    return new UserSelectionUseCases(currentUser, userSelections);
  }
}
