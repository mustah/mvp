package com.elvaco.mvp.testdata;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import com.elvaco.mvp.configuration.config.properties.MvpProperties;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.database.repository.jpa.DashboardJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewaysMetersJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PropertiesJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.WidgetJpaRepository;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:it.properties")
public abstract class IntegrationTest implements ContextDsl {

  private static final long MAX_WAIT_TIME = TimeUnit.SECONDS.toNanos(15);

  private static final Map<String, Integer> USED_CONTEXT_NAMES = new HashMap<>();

  @Autowired
  protected MvpProperties mvpProperties;

  @Autowired
  protected OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  protected UserSelectionJpaRepository userSelectionJpaRepository;

  @Autowired
  protected LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  protected PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  protected PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  protected GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  protected GatewaysMetersJpaRepository gatewaysMetersJpaRepository;

  @Autowired
  protected GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @Autowired
  protected MeasurementJpaRepository measurementJpaRepository;

  @Autowired
  protected MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;

  @Autowired
  protected PropertiesJpaRepository propertiesJpaRepository;

  @Autowired
  protected MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  @Autowired
  protected DashboardJpaRepository dashboardJpaRepository;

  @Autowired
  protected WidgetJpaRepository widgetJpaRepository;

  @Autowired
  protected Users users;

  @Autowired
  protected UserSelections userSelections;

  @Autowired
  protected Measurements measurements;

  @Autowired
  protected Organisations organisations;

  @Autowired
  protected OrganisationAssets organisationAssets;

  @Autowired
  protected LogicalMeters logicalMeters;

  @Autowired
  protected PhysicalMeters physicalMeters;

  @Autowired
  protected MeterStatusLogs meterStatusLogs;

  @Autowired
  protected MeterAlarmLogs meterAlarmLogs;

  @Autowired
  protected MeterDefinitions meterDefinitions;

  @Autowired
  protected Gateways gateways;

  @Autowired
  protected GatewayStatusLogs gatewayStatusLogs;

  @Autowired
  protected Dashboards dashboards;

  @Autowired
  protected Widgets widgets;

  @Autowired
  protected MediumProvider mediumProvider;

  @Autowired
  protected QuantityProvider quantityProvider;

  @Autowired
  protected SystemMeterDefinitionProvider systemMeterDefinitionProvider;

  @Autowired
  protected TokenFactory tokenFactory;

  @Autowired
  protected TokenService tokenService;

  @Autowired
  private CacheManager ehCacheManager;

  @Value("${mvp.measurement.stat.queue.age:1}")
  private int statQueueAge;

  private IntegrationTestFixtureContextFactory integrationTestFixtureContextFactory;

  @LocalServerPort
  private int serverPort;

  private IntegrationTestFixtureContext context;

  private RestClient restClient;

  @After
  public final void tearDownBase() {
    try {
      restClient().logout();
      removeEntities();
      afterRemoveEntitiesHook();
      removeSubOrganisationsEntities();
      destroyContext();
      SecurityContextHolder.clearContext();
    } catch (JpaSystemException e) {
      log.warn("Exceptions should be ignored here, since most of them are transactions related", e);
    }
  }

  @Override
  public IntegrationTestFixtureContext context() {
    if (context == null) {
      context = newContext(getCallingTestCaseName());
    }
    return context;
  }

  protected void afterRemoveEntitiesHook() {}

  protected void authenticate(User user) {
    AuthenticatedUser authenticatedUser = new MvpUserDetails(user, tokenFactory.newToken());
    tokenService.saveToken(authenticatedUser);
    SecurityContextHolder.getContext()
      .setAuthentication(AuthenticationToken.from(authenticatedUser));
  }

  protected RestClient restClient() {
    if (restClient == null) {
      restClient = new RestClient(serverPort);
    }
    return restClient;
  }

  protected RestClient asNotLoggedIn() {
    return restClient();
  }

  protected RestClient asMvpUser() {
    return restClientWith(context().mvpUser);
  }

  protected RestClient asMvpAdmin() {
    return restClientWith(context().mvpAdmin);
  }

  protected RestClient asOtcAdmin() {
    return restClientWith(context().otcAdmin);
  }

  protected RestClient asSuperAdmin() {
    return restClientWith(context().superAdmin);
  }

  protected RestClient as(User user) {
    return restClientWith(user);
  }

  protected User createUserIfNotPresent(User user) {
    return users
      .findByEmail(user.email)
      .orElseGet(() -> users.save(user));
  }

  protected RestClient restClientWith(User user) {
    createUserIfNotPresent(user);
    return restClient()
      .loginWith(user.getUsername(), user.password)
      .tokenAuthorization();
  }

  /* Use with caution. Is there *any* way for you to check your assertion besides continuous
  polling? Then do that instead! */
  protected boolean waitForCondition(BooleanSupplier condition) throws InterruptedException {
    long start = System.nanoTime();
    do {
      if (condition.getAsBoolean()) {
        return true;
      }
      Thread.sleep(100);
    } while (System.nanoTime() < (start + MAX_WAIT_TIME));
    return false;
  }

  protected void addMeasurementsForMeterQuantities(
    PhysicalMeter physicalMeter,
    LogicalMeter logicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime when,
    double value
  ) {
    for (Quantity quantity : quantities) {
      measurements.save(
        Measurement.builder()
          .readoutTime(when)
          .quantity(quantity.name)
          .value(value)
          .unit(quantity.storageUnit)
          .physicalMeter(physicalMeter)
          .build(),
        logicalMeter

      );
    }
  }

  protected final void waitForMeasurementStat() {
    try {
      Thread.sleep(statQueueAge + 100);
    } catch (InterruptedException ignore) { }
  }

  private void destroyContext() {
    if (context != null) {
      getIntegrationTestFixtureContextFactory().destroy(context);
      context = null;
    }
  }

  private void removeEntities() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewaysMetersJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    dashboardJpaRepository.deleteAll();
    widgetJpaRepository.deleteAll();
    organisationAssets.deleteAll();
    removeNonSystemMeterDefinitions();
    ehCacheManager.getCacheNames().stream()
      .map(name -> ehCacheManager.getCache(name))
      .filter(Objects::nonNull)
      .forEach(Cache::clear);
  }

  private void removeNonSystemMeterDefinitions() {
    meterDefinitionJpaRepository.findAll()
      .stream()
      .filter(md -> md.organisation != null)
      .forEach(md -> meterDefinitionJpaRepository.deleteById(md.id));
  }

  private void removeSubOrganisationsEntities() {
    String rootOrgSlug = mvpProperties.getRootOrganisation().getSlug();
    organisationJpaRepository.findAll().stream()
      .filter(organisation -> !organisation.slug.equals(rootOrgSlug))
      .sorted((o1, o2) -> {
        if (o1.parent != null) {
          return o2.parent == null ? -1 : 0;
        } else if (o2.parent != null) {
          return 1;
        }
        return 0;
      })
      .map(Identifiable::getId)
      .forEach(organisationJpaRepository::deleteById);

    userSelectionJpaRepository.deleteAll();
  }

  private String getCallingTestCaseName() {
    return Stream.of(Thread.currentThread().getStackTrace())
      .filter(ste -> ste.getClassName().endsWith("Test"))
      .filter(ste -> ste.getClassName().equalsIgnoreCase(this.getClass().getName()))
      .findFirst()
      .map(ste -> testCaseToContextName(ste.getClassName(), ste.getMethodName()))
      .orElse("UnknownTestSuite");
  }

  private String testCaseToContextName(String className, String methodName) {
    String contextNamePrefix = className + "-" + methodName;
    return contextNamePrefix + "-" + USED_CONTEXT_NAMES.compute(
      contextNamePrefix,
      (k, v) -> v == null ? 1 : v + 1
    );
  }

  private IntegrationTestFixtureContext newContext(String identifier) {
    return getIntegrationTestFixtureContextFactory().create(identifier);
  }

  private IntegrationTestFixtureContextFactory getIntegrationTestFixtureContextFactory() {
    if (integrationTestFixtureContextFactory == null) {
      integrationTestFixtureContextFactory = new IntegrationTestFixtureContextFactory(
        organisations,
        users,
        userSelections,
        logicalMeters,
        physicalMeters,
        meterStatusLogs,
        meterAlarmLogs,
        measurements,
        gateways,
        gatewayStatusLogs,
        meterDefinitions,
        dashboards,
        widgets
      );
    }
    return integrationTestFixtureContextFactory;
  }
}
