package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.Measurements;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.core.usecase.Settings;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.repository.access.MeasurementMapper;
import com.elvaco.mvp.repository.access.MeasurementRepository;
import com.elvaco.mvp.repository.access.MeteringPointMapper;
import com.elvaco.mvp.repository.access.MeteringPointRepository;
import com.elvaco.mvp.repository.access.OrganisationMapper;
import com.elvaco.mvp.repository.access.SettingMapper;
import com.elvaco.mvp.repository.access.SettingRepository;
import com.elvaco.mvp.repository.access.UserMapper;
import com.elvaco.mvp.repository.access.UserRepository;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;
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
  private final MeteringPointJpaRepository meteringPointJpaRepository;
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
    MeteringPointJpaRepository meteringPointJpaRepository
  ) {
    this.userJpaRepository = userJpaRepository;
    this.settingJpaRepository = settingJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.modelMapper = modelMapper;
    this.passwordEncoder = passwordEncoder;
    this.meteringPointJpaRepository = meteringPointJpaRepository;
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
  MeteringPoints meteringPoints() {
    return new MeteringPointRepository(
      meteringPointJpaRepository,
      new MeteringPointMapper()
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
