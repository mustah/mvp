package com.elvaco.mvp.configuration.config;

import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionData;
import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.configuration.config.properties.MvpProperties;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.core.spi.repository.Quantities;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.repository.access.GatewayRepository;
import com.elvaco.mvp.database.repository.access.GatewayStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.LocationRepository;
import com.elvaco.mvp.database.repository.access.LogicalMeterRepository;
import com.elvaco.mvp.database.repository.access.MeasurementRepository;
import com.elvaco.mvp.database.repository.access.MeterAlarmLogsRepository;
import com.elvaco.mvp.database.repository.access.MeterDefinitionRepository;
import com.elvaco.mvp.database.repository.access.MeterStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.OrganisationRepository;
import com.elvaco.mvp.database.repository.access.PhysicalMetersRepository;
import com.elvaco.mvp.database.repository.access.PropertiesRepository;
import com.elvaco.mvp.database.repository.access.QuantityRepository;
import com.elvaco.mvp.database.repository.access.RoleRepository;
import com.elvaco.mvp.database.repository.access.RootOrganisationRepository;
import com.elvaco.mvp.database.repository.access.SettingRepository;
import com.elvaco.mvp.database.repository.access.UserRepository;
import com.elvaco.mvp.database.repository.access.UserSelectionRepository;
import com.elvaco.mvp.database.repository.jpa.DisplayQuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MapMarkerJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PropertiesJpaRepository;
import com.elvaco.mvp.database.repository.jpa.QuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SummaryJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(MvpProperties.class)
class DataProviderConfig {

  private final PasswordEncoder passwordEncoder;
  private final UserJpaRepository userJpaRepository;
  private final SettingJpaRepository settingJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LocationJpaRepository locationJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;
  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final DisplayQuantityJpaRepository displayQuantityJpaRepository;
  private final OrganisationJpaRepository organisationJpaRepository;
  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;
  private final GatewayJpaRepository gatewayJpaRepository;
  private final RoleJpaRepository roleJpaRepository;
  private final UserSelectionJpaRepository userSelectionJpaRepository;
  private final MapMarkerJpaRepository logicalMeterMapMarkerJooqJpaRepository;
  private final MapMarkerJpaRepository gatewayMapMarkerJooqJpaRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final PropertiesJpaRepository propertiesJpaRepository;
  private final QuantityJpaRepository quantityJpaRepository;
  private final MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;
  private final UnitConverter unitConverter;
  private final EntityManager entityManager;

  @Bean
  Users users(
    ProductionDataProvider productionDataProvider,
    Organisations organisations,
    Roles roles
  ) {
    // the organisations & roles must be loaded, because the super admin user is associated to an
    // already saved organisation
    var users = new UserRepository(userJpaRepository, passwordEncoder::encode);

    User user = productionDataProvider.superAdminUser();
    if (!users.findByEmail(user.email).isPresent()) {
      users.save(user);
    }

    return users;
  }

  @Bean
  Settings settings() {
    return new SettingRepository(settingJpaRepository);
  }

  @Bean
  LogicalMeters logicalMeters(
    LogicalMeterEntityMapper logicalMeterEntityMapper,
    MeterDefinitions meterDefinitions
  ) {
    return new LogicalMeterRepository(
      logicalMeterJpaRepository,
      summaryJpaRepository,
      logicalMeterEntityMapper,
      meterDefinitions
    );
  }

  @Bean
  Locations locations() {
    return new LocationRepository(
      locationJpaRepository,
      logicalMeterMapMarkerJooqJpaRepository,
      gatewayMapMarkerJooqJpaRepository
    );
  }

  @Bean
  Measurements measurements(
    DSLContext dsl,
    QuantityProvider quantityProvider,
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new MeasurementRepository(
      dsl,
      measurementJpaRepository,
      quantityProvider,
      unitConverter,
      quantityEntityMapper
    );
  }

  @Bean
  PhysicalMeters physicalMeters() {
    return new PhysicalMetersRepository(physicalMeterJpaRepository, entityManager);
  }

  @Bean
  Organisations organisations(ProductionDataProvider productionDataProvider) {
    var organisations = new OrganisationRepository(organisationJpaRepository);

    productionDataProvider.organisations()
      .stream()
      .filter(organisation -> !organisations.findBySlug(organisation.slug).isPresent())
      .forEach(organisations::save);

    return organisations;
  }

  @Bean
  RootOrganisationRepository rootOrganisationRepository(
    OrganisationJpaRepository organisationJpaRepository
  ) {
    return new RootOrganisationRepository(organisationJpaRepository);
  }

  @Bean
  Organisation rootOrganisation(
    RootOrganisationRepository rootOrganisationRepository,
    MvpProperties mvpProperties
  ) {
    MvpProperties.RootOrganisation rootOrg = mvpProperties.getRootOrganisation();
    return rootOrganisationRepository
      .findBySlug(rootOrg.getSlug())
      .orElseGet(() -> rootOrganisationRepository.save(new Organisation(
        UUID.randomUUID(),
        rootOrg.getName(),
        rootOrg.getSlug(),
        rootOrg.getName()
      )));
  }

  @Bean
  ProductionDataProvider productionData(
    MvpProperties mvpProperties,
    Organisation rootOrganisation
  ) {
    MvpProperties.Superadmin superadmin = mvpProperties.getSuperadmin();
    return new ProductionData(
      superadmin.getEmail(),
      superadmin.getPassword(),
      rootOrganisation
    );
  }

  @Bean
  Roles roles(ProductionDataProvider productionDataProvider) {
    var roleRepository = new RoleRepository(roleJpaRepository);
    roleRepository.save(productionDataProvider.roles());
    return roleRepository;
  }

  @Bean
  MeterDefinitions meterDefinitions(
    MeterDefinitionEntityMapper meterDefinitionEntityMapper
  ) {
    return new MeterDefinitionRepository(
      meterDefinitionJpaRepository,
      displayQuantityJpaRepository,
      meterDefinitionEntityMapper
    );
  }

  @Bean
  Gateways gateways(GatewayWithMetersMapper gatewayWithMetersMapper) {
    return new GatewayRepository(gatewayJpaRepository, gatewayWithMetersMapper);
  }

  @Bean
  UserSelections selections() {
    return new UserSelectionRepository(userSelectionJpaRepository);
  }

  @Bean
  MeterStatusLogs meterStatusLog() {
    return new MeterStatusLogsRepository(physicalMeterStatusLogJpaRepository);
  }

  @Bean
  MeterAlarmLogs meterAlarmLogs() {
    return new MeterAlarmLogsRepository(meterAlarmLogJpaRepository);
  }

  @Bean
  GatewayStatusLogs gatewayStatusLogs() {
    return new GatewayStatusLogsRepository(gatewayStatusLogJpaRepository);
  }

  @Bean
  Properties properties() {
    return new PropertiesRepository(propertiesJpaRepository);
  }

  @Bean
  Quantities quantities(QuantityEntityMapper quantityEntityMapper) {
    return new QuantityRepository(quantityJpaRepository, quantityEntityMapper);
  }
}
