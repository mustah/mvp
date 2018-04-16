package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionData;
import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.access.GatewayRepository;
import com.elvaco.mvp.database.repository.access.GatewayStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.LocationRepository;
import com.elvaco.mvp.database.repository.access.LogicalMeterRepository;
import com.elvaco.mvp.database.repository.access.MeasurementRepository;
import com.elvaco.mvp.database.repository.access.MeterDefinitionRepository;
import com.elvaco.mvp.database.repository.access.MeterStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.OrganisationRepository;
import com.elvaco.mvp.database.repository.access.PhysicalMetersRepository;
import com.elvaco.mvp.database.repository.access.RoleRepository;
import com.elvaco.mvp.database.repository.access.SettingRepository;
import com.elvaco.mvp.database.repository.access.UserRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayStatusLogMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LocationMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogMapper;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.SettingMapper;
import com.elvaco.mvp.database.repository.mappers.UserMapper;
import com.elvaco.mvp.database.repository.queryfilters.GatewayQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.GatewayStatusLogQueryFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class DataProviderConfig {

  private final PasswordEncoder passwordEncoder;
  private final UserJpaRepository userJpaRepository;
  private final SettingJpaRepository settingJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LocationJpaRepository locationJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;
  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final OrganisationJpaRepository organisationJpaRepository;
  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;
  private final GatewayJpaRepository gatewayJpaRepository;
  private final RoleJpaRepository roleJpaRepository;

  @Autowired
  DataProviderConfig(
    PasswordEncoder passwordEncoder,
    UserJpaRepository userJpaRepository,
    SettingJpaRepository settingJpaRepository,
    LocationJpaRepository locationJpaRepository,
    MeasurementJpaRepository measurementJpaRepository,
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    PhysicalMeterJpaRepository physicalMeterJpaRepository,
    MeterDefinitionJpaRepository meterDefinitionJpaRepository,
    OrganisationJpaRepository organisationJpaRepository,
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository,
    GatewayJpaRepository gatewayJpaRepository,
    RoleJpaRepository roleJpaRepository
  ) {
    this.userJpaRepository = userJpaRepository;
    this.settingJpaRepository = settingJpaRepository;
    this.locationJpaRepository = locationJpaRepository;
    this.measurementJpaRepository = measurementJpaRepository;
    this.passwordEncoder = passwordEncoder;
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.physicalMeterJpaRepository = physicalMeterJpaRepository;
    this.meterDefinitionJpaRepository = meterDefinitionJpaRepository;
    this.organisationJpaRepository = organisationJpaRepository;
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.gatewayStatusLogJpaRepository = gatewayStatusLogJpaRepository;
    this.gatewayJpaRepository = gatewayJpaRepository;
    this.roleJpaRepository = roleJpaRepository;
  }

  @Bean
  Users users() {
    return new UserRepository(
      userJpaRepository,
      new UserMapper(new OrganisationMapper()),
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
      physicalMeterStatusLogJpaRepository,
      new LogicalMeterSortingMapper(),
      newLogicalMeterMapper(),
      measurementJpaRepository
    );
  }

  @Bean
  Locations locations() {
    return new LocationRepository(locationJpaRepository, new LocationMapper());
  }

  @Bean
  Measurements measurements() {
    return new MeasurementRepository(
      measurementJpaRepository,
      new MeasurementMapper(newPhysicalMeterMapper())
    );
  }

  @Bean
  PhysicalMeters physicalMeters() {
    return new PhysicalMetersRepository(
      physicalMeterJpaRepository,
      newPhysicalMeterMapper()
    );
  }

  @Bean
  Organisations organisations() {
    return new OrganisationRepository(
      organisationJpaRepository,
      new OrganisationMapper()
    );
  }

  @Bean
  ProductionDataProvider productionData(
    @Value("${mvp.superadmin.email}") String superAdminEmail,
    @Value("${mvp.superadmin.password}") String superAdminPassword
  ) {
    return new ProductionData(superAdminEmail, superAdminPassword);
  }

  @Bean
  Roles roles() {
    return new RoleRepository(roleJpaRepository);
  }

  @Bean
  MeterDefinitions meterDefinitions() {
    return new MeterDefinitionRepository(meterDefinitionJpaRepository, new MeterDefinitionMapper());
  }

  @Bean
  Gateways gateways() {
    return new GatewayRepository(
      gatewayJpaRepository,
      new GatewayQueryFilters(),
      new GatewayMapper(),
      new GatewayWithMetersMapper(newLogicalMeterMapper(), new GatewayStatusLogMapper()),
      gatewayStatusLogJpaRepository,
      new GatewayStatusLogQueryFilters()
    );
  }

  @Bean
  MeterStatusLogs meterStatusLog() {
    return new MeterStatusLogsRepository(
      physicalMeterStatusLogJpaRepository,
      new MeterStatusLogMapper()
    );
  }

  @Bean
  GatewayStatusLogs gatewayStatusLogs() {
    return new GatewayStatusLogsRepository(
      gatewayStatusLogJpaRepository,
      new GatewayStatusLogMapper()
    );
  }

  private LogicalMeterMapper newLogicalMeterMapper() {
    return new LogicalMeterMapper(
      new MeterDefinitionMapper(),
      new LocationMapper(),
      newPhysicalMeterMapper(),
      new GatewayMapper()
    );
  }

  private PhysicalMeterMapper newPhysicalMeterMapper() {
    return new PhysicalMeterMapper(new OrganisationMapper(), new MeterStatusLogMapper());
  }
}
