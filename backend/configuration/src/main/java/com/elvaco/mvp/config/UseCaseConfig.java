package com.elvaco.mvp.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.Measurements;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.core.usecase.MeteringPointsUseCases;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.Settings;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.core.usecase.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UseCaseConfig {

  private final Users users;
  private final MeteringPoints meteringPoints;
  private final Settings settings;
  private final Measurements measurements;

  @Autowired
  UseCaseConfig(
    Users users,
    Settings settings,
    MeteringPoints meteringPoints,
    Measurements measurements
  ) {
    this.users = users;
    this.meteringPoints = meteringPoints;
    this.settings = settings;
    this.measurements = measurements;
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
  MeteringPointsUseCases meteringPointsUseCases() {
    return new MeteringPointsUseCases(meteringPoints);
  }

  @Bean
  MeasurementUseCases measurementUseCases(AuthenticatedUser currentUser) {
    return new MeasurementUseCases(currentUser, measurements);
  }
}
