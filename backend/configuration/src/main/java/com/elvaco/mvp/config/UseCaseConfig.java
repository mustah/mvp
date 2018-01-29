package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.Measurements;
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
  private final Settings settings;
  private final Measurements measurements;

  @Autowired
  UseCaseConfig(Users users, Settings settings, Measurements measurements) {
    this.users = users;
    this.settings = settings;
    this.measurements = measurements;
  }

  @Bean
  SettingUseCases settingUseCases() {
    return new SettingUseCases(settings);
  }

  @Bean
  UserUseCases userUseCases() {
    return new UserUseCases(users);
  }

  @Bean
  MeasurementUseCases measurementUseCases() {
    return new MeasurementUseCases(measurements);
  }
}
