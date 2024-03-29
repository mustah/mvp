package com.elvaco.mvp.configuration.config;

import java.io.IOException;
import java.util.Map;
import javax.persistence.EntityManager;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionData;
import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.configuration.config.properties.MvpProperties;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.AlarmDescriptions;
import com.elvaco.mvp.core.spi.repository.CollectionStats;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.GatewaysMeters;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.core.spi.repository.Quantities;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;
import com.elvaco.mvp.database.repository.access.AlarmDescriptionsRepository;
import com.elvaco.mvp.database.repository.access.CollectionStatsRepository;
import com.elvaco.mvp.database.repository.access.DashboardRepository;
import com.elvaco.mvp.database.repository.access.GatewayRepository;
import com.elvaco.mvp.database.repository.access.GatewayStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.GatewaysMetersRepository;
import com.elvaco.mvp.database.repository.access.LocationRepository;
import com.elvaco.mvp.database.repository.access.LogicalMeterRepository;
import com.elvaco.mvp.database.repository.access.MeasurementRepository;
import com.elvaco.mvp.database.repository.access.MeterAlarmLogsRepository;
import com.elvaco.mvp.database.repository.access.MeterDefinitionRepository;
import com.elvaco.mvp.database.repository.access.MeterStatusLogsRepository;
import com.elvaco.mvp.database.repository.access.OrganisationAssetRepository;
import com.elvaco.mvp.database.repository.access.OrganisationRepository;
import com.elvaco.mvp.database.repository.access.OrganisationThemeRepository;
import com.elvaco.mvp.database.repository.access.PhysicalMetersRepository;
import com.elvaco.mvp.database.repository.access.PropertiesRepository;
import com.elvaco.mvp.database.repository.access.QuantityRepository;
import com.elvaco.mvp.database.repository.access.RoleRepository;
import com.elvaco.mvp.database.repository.access.RootOrganisationRepository;
import com.elvaco.mvp.database.repository.access.SettingRepository;
import com.elvaco.mvp.database.repository.access.UserRepository;
import com.elvaco.mvp.database.repository.access.UserSelectionRepository;
import com.elvaco.mvp.database.repository.access.WidgetRepository;
import com.elvaco.mvp.database.repository.jooq.AlarmDescriptionsJooqRepository;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jpa.DashboardJpaRepository;
import com.elvaco.mvp.database.repository.jpa.DisplayQuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewaysMetersJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MapMarkerJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationAssetJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationThemeJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PropertiesJpaRepository;
import com.elvaco.mvp.database.repository.jpa.QuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SummaryJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.WidgetJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MediumEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(MvpProperties.class)
@Slf4j
class DataProviderConfig {

  private static final byte[] EMPTY_CONTENT = {};

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
  private final OrganisationAssetJpaRepository organisationAssetJpaRepository;
  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;
  private final GatewayJpaRepository gatewayJpaRepository;
  private final GatewaysMetersJpaRepository gatewaysMetersJpaRepository;
  private final RoleJpaRepository roleJpaRepository;
  private final UserSelectionJpaRepository userSelectionJpaRepository;
  private final MapMarkerJpaRepository logicalMeterMapMarkerJooqJpaRepository;
  private final MapMarkerJpaRepository gatewayMapMarkerJooqJpaRepository;
  private final AlarmDescriptionsJooqRepository alarmDescriptionsJooqRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final PropertiesJpaRepository propertiesJpaRepository;
  private final QuantityJpaRepository quantityJpaRepository;
  private final DashboardJpaRepository dashboardJpaRepository;
  private final WidgetJpaRepository widgetJpaRepository;
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
    if (users.findByEmail(user.email).isEmpty()) {
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
    LogicalMeterEntityMapper logicalMeterEntityMapper
  ) {
    return new LogicalMeterRepository(
      logicalMeterJpaRepository,
      summaryJpaRepository,
      logicalMeterEntityMapper
    );
  }

  @Bean
  CollectionStats collectionStats(DSLContext dsl, MeasurementThresholdParser parser) {
    return new CollectionStatsRepository(dsl, parser);
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
    FilterAcceptor logicalMeterMeasurementFilters,
    MeasurementEntityMapper measurementEntityMapper
  ) {
    return new MeasurementRepository(
      dsl,
      measurementJpaRepository,
      quantityProvider,
      unitConverter,
      logicalMeterMeasurementFilters,
      measurementEntityMapper
    );
  }

  @Bean
  PhysicalMeters physicalMeters() {
    return new PhysicalMetersRepository(physicalMeterJpaRepository, entityManager);
  }

  @Bean
  Organisations organisations(ProductionDataProvider productionDataProvider) {
    var organisations = new OrganisationRepository(organisationJpaRepository, entityManager);

    productionDataProvider.organisations().stream()
      .filter(organisation -> organisations.findBySlug(organisation.slug).isEmpty())
      .forEach(organisations::saveAndFlush);

    return organisations;
  }

  @Bean
  OrganisationAssets organisationAssets() {
    return new OrganisationAssetRepository(
      Map.of(
        AssetType.LOGOTYPE, Asset.builder()
          .assetType(AssetType.LOGOTYPE)
          .contentType("image/svg+xml")
          .content(getBytesFromClassPathResource(
            AssetType.LOGOTYPE,
            "assets/logotype.svg"
          ))
          .build(),
        AssetType.LOGIN_BACKGROUND, Asset.builder()
          .assetType(AssetType.LOGIN_BACKGROUND)
          .contentType("image/jpeg")
          .content(getBytesFromClassPathResource(
            AssetType.LOGIN_BACKGROUND,
            "assets/login_background.jpg"
          ))
          .build(),
        AssetType.LOGIN_LOGOTYPE, Asset.builder()
          .assetType(AssetType.LOGIN_LOGOTYPE)
          .contentType("image/svg+xml")
          .content(getBytesFromClassPathResource(
            AssetType.LOGIN_LOGOTYPE,
            "assets/login_logotype.svg"
          ))
          .build()
      ),
      organisationAssetJpaRepository
    );
  }

  @Bean
  OrganisationThemes organisationTheme(
    OrganisationThemeJpaRepository organisationThemeJpaRepository
  ) {
    return new OrganisationThemeRepository(organisationThemeJpaRepository);
  }

  @Bean
  RootOrganisationRepository rootOrganisationRepository(
    OrganisationJpaRepository organisationJpaRepository
  ) {
    return new RootOrganisationRepository(organisationJpaRepository);
  }

  @Bean
  AlarmDescriptions alarmDescriptions() {
    return new AlarmDescriptionsRepository(alarmDescriptionsJooqRepository);
  }

  @Bean
  Organisation rootOrganisation(
    RootOrganisationRepository rootOrganisationRepository,
    MvpProperties mvpProperties
  ) {
    MvpProperties.RootOrganisation rootOrg = mvpProperties.getRootOrganisation();
    return rootOrganisationRepository
      .findBySlug(rootOrg.getSlug())
      .orElseGet(() -> rootOrganisationRepository.save(Organisation.of(rootOrg.getName())));
  }

  @Bean
  ProductionDataProvider productionData(
    MvpProperties mvpProperties,
    Organisation rootOrganisation
  ) {
    MvpProperties.Superadmin superAdmin = mvpProperties.getSuperadmin();
    return new ProductionData(
      superAdmin.getEmail(),
      superAdmin.getPassword(),
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
    MeterDefinitionEntityMapper meterDefinitionEntityMapper,
    MediumEntityMapper mediumEntityMapper
  ) {
    return new MeterDefinitionRepository(
      meterDefinitionJpaRepository,
      displayQuantityJpaRepository,
      meterDefinitionEntityMapper,
      mediumEntityMapper
    );
  }

  @Bean
  Gateways gateways(GatewayWithMetersMapper gatewayWithMetersMapper) {
    return new GatewayRepository(gatewayJpaRepository, gatewayWithMetersMapper);
  }

  @Bean
  GatewaysMeters gatewaysMeters() {
    return new GatewaysMetersRepository(gatewaysMetersJpaRepository);
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
    return new MeterAlarmLogsRepository(meterAlarmLogJpaRepository, measurementJpaRepository);
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

  @Bean
  Dashboards dashboards() {
    return new DashboardRepository(dashboardJpaRepository);
  }

  @Bean
  Widgets widgets() {
    return new WidgetRepository(widgetJpaRepository);
  }

  private byte[] getBytesFromClassPathResource(AssetType lookingFor, String path) {
    try {
      return new ClassPathResource(path)
        .getInputStream()
        .readAllBytes();
    } catch (IOException e) {
      log.warn("Found no default {}, looking at: {}", lookingFor, path);
      // this will make the front end render "empty" images, which is better than just dying
      return EMPTY_CONTENT;
    }
  }
}
