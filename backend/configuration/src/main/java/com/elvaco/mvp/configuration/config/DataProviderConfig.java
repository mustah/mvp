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
import com.elvaco.mvp.core.spi.repository.UserSelections;
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
import com.elvaco.mvp.database.repository.access.UserSelectionRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MapMarkerJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.GatewayQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.GatewayStatusLogQueryFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
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
  private final UserSelectionJpaRepository userSelectionJpaRepository;
  private final MapMarkerJpaRepository meterMapQueryDslJpaRepository;
  private final MapMarkerJpaRepository gatewayMapQueryDslJpaRepository;

  @Bean
  Users users() {
    return new UserRepository(
      userJpaRepository,
      passwordEncoder::encode
    );
  }

  @Bean
  Settings settings() {
    return new SettingRepository(
      settingJpaRepository
    );
  }

  @Bean
  LogicalMeters logicalMeters() {
    return new LogicalMeterRepository(
      logicalMeterJpaRepository,
      new LogicalMeterSortingEntityMapper()
    );
  }

  @Bean
  Locations locations() {
    return new LocationRepository(
      locationJpaRepository,
      meterMapQueryDslJpaRepository,
      gatewayMapQueryDslJpaRepository
    );
  }

  @Bean
  Measurements measurements() {
    return new MeasurementRepository(
      measurementJpaRepository
    );
  }

  @Bean
  PhysicalMeters physicalMeters() {
    return new PhysicalMetersRepository(
      physicalMeterJpaRepository
    );
  }

  @Bean
  Organisations organisations() {
    return new OrganisationRepository(
      organisationJpaRepository
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
    return new MeterDefinitionRepository(meterDefinitionJpaRepository);
  }

  @Bean
  Gateways gateways() {
    return new GatewayRepository(
      gatewayJpaRepository,
      new GatewayQueryFilters(),
      new GatewayStatusLogQueryFilters(),
      gatewayStatusLogJpaRepository
    );
  }

  @Bean
  UserSelections selections() {
    return new UserSelectionRepository(
      userSelectionJpaRepository
    );
  }

  @Bean
  MeterStatusLogs meterStatusLog() {
    return new MeterStatusLogsRepository(
      physicalMeterStatusLogJpaRepository
    );
  }

  @Bean
  GatewayStatusLogs gatewayStatusLogs() {
    return new GatewayStatusLogsRepository(
      gatewayStatusLogJpaRepository
    );
  }
}
