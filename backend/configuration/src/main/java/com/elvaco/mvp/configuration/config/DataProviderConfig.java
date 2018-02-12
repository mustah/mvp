package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.access.LogicalMeterRepository;
import com.elvaco.mvp.database.repository.access.MeasurementRepository;
import com.elvaco.mvp.database.repository.access.MeterDefinitionRepository;
import com.elvaco.mvp.database.repository.access.OrganisationRepository;
import com.elvaco.mvp.database.repository.access.PhysicalMetersRepository;
import com.elvaco.mvp.database.repository.access.SettingRepository;
import com.elvaco.mvp.database.repository.access.UserRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LocationMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.SettingMapper;
import com.elvaco.mvp.database.repository.mappers.UserMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class DataProviderConfig {

  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserJpaRepository userJpaRepository;
  private final SettingJpaRepository settingJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;
  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  DataProviderConfig(
    ModelMapper modelMapper,
    PasswordEncoder passwordEncoder,
    UserJpaRepository userJpaRepository,
    SettingJpaRepository settingJpaRepository,
    MeasurementJpaRepository measurementJpaRepository,
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    PhysicalMeterJpaRepository physicalMeterJpaRepository,
    MeterDefinitionJpaRepository meterDefinitionJpaRepository,
    OrganisationJpaRepository organisationJpaRepository
  ) {
    this.userJpaRepository = userJpaRepository;
    this.settingJpaRepository = settingJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.modelMapper = modelMapper;
    this.passwordEncoder = passwordEncoder;
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.physicalMeterJpaRepository = physicalMeterJpaRepository;
    this.meterDefinitionJpaRepository = meterDefinitionJpaRepository;
    this.organisationJpaRepository = organisationJpaRepository;
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
      newLogicalMeterMapper()
    );
  }

  @Bean
  Measurements measurements() {
    return new MeasurementRepository(
      measurementJpaRepository,
      new MeasurementFilterToPredicateMapper(),
      new MeasurementMapper(newPhysicalMeterMapper())
    );
  }

  @Bean
  PhysicalMeters physicalMeters() {
    return new PhysicalMetersRepository(physicalMeterJpaRepository, newPhysicalMeterMapper());
  }

  @Bean
  Organisations organisations() {
    return new OrganisationRepository(
      organisationJpaRepository,
      new OrganisationMapper()
    );
  }

  @Bean
  MeterDefinitions meterDefinitions() {
    return new MeterDefinitionRepository(meterDefinitionJpaRepository, new MeterDefinitionMapper());
  }

  private LogicalMeterMapper newLogicalMeterMapper() {
    return new LogicalMeterMapper(
      new MeterDefinitionMapper(),
      new LocationMapper(),
      newPhysicalMeterMapper()
    );
  }

  private PhysicalMeterMapper newPhysicalMeterMapper() {
    return new PhysicalMeterMapper(new OrganisationMapper());
  }
}
