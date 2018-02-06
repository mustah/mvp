package com.elvaco.mvp.config;

import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.repository.access.LocationMapper;
import com.elvaco.mvp.repository.access.LogicalMeterMapper;
import com.elvaco.mvp.repository.access.LogicalMeterRepository;
import com.elvaco.mvp.repository.access.MeasurementMapper;
import com.elvaco.mvp.repository.access.MeasurementRepository;
import com.elvaco.mvp.repository.access.OrganisationMapper;
import com.elvaco.mvp.repository.access.SettingMapper;
import com.elvaco.mvp.repository.access.SettingRepository;
import com.elvaco.mvp.repository.access.UserMapper;
import com.elvaco.mvp.repository.access.UserRepository;
import com.elvaco.mvp.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.LogicalMeterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class DataProviderConfig {

  private final UserJpaRepository userJpaRepository;
  private final SettingJpaRepository settingJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final MeasurementJpaRepository measurementJpaRepository;

  @Autowired
  DataProviderConfig(
    UserJpaRepository userJpaRepository,
    SettingJpaRepository settingJpaRepository,
    MeasurementJpaRepository measurementJpaRepository,
    ModelMapper modelMapper,
    PasswordEncoder passwordEncoder,
    LogicalMeterJpaRepository logicalMeterJpaRepository
  ) {
    this.userJpaRepository = userJpaRepository;
    this.settingJpaRepository = settingJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.modelMapper = modelMapper;
    this.passwordEncoder = passwordEncoder;
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
  }

  @Bean
  Users users() {
    OrganisationMapper organisationMapper = new OrganisationMapper();
    return new UserRepository(
      userJpaRepository,
      new UserMapper(modelMapper, organisationMapper),
      organisationMapper,
      passwordEncoder::encode
    );
  }

  @Bean
  Settings settings() {
    return new SettingRepository(
      settingJpaRepository,
      new SettingMapper()
    );
  }

  @Bean
  LogicalMeters logicalMeters() {
    return new LogicalMeterRepository(
      logicalMeterJpaRepository,
      new LogicalMeterToPredicateMapper(),
      new LogicalMeterMapper(
        new LocationMapper()
      )
    );
  }

  @Bean
  Measurements measurements() {
    return new MeasurementRepository(
      measurementJpaRepository,
      new MeasurementFilterToPredicateMapper(),
      new MeasurementMapper(modelMapper, new OrganisationMapper())
    );
  }
}
