package com.elvaco.mvp.testdata;

import java.util.function.BooleanSupplier;
import javax.persistence.EntityManagerFactory;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.OTHER_USER;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

  private static final long MAX_WAIT_TIME = 15_000;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private Users users;

  @Autowired
  private EntityManagerFactory factory;

  @Autowired
  private TokenFactory tokenFactory;

  @Autowired
  private TokenService tokenService;

  private IntegrationTestFixtureContextFactory integrationTestFixtureContextFactory;

  @LocalServerPort
  private int serverPort;

  private IntegrationTestFixtureContext context;
  private RestClient restClient;

  @After
  public final void tearDownBase() {
    restClient().logout();
    if (context != null) {
      getIntegrationTestFixtureContextFactory().destroy(context);
      context = null;
    }
    SecurityContextHolder.clearContext();
  }

  protected boolean isPostgresDialect() {
    return ((SessionFactoryImplementor) factory.unwrap(SessionFactory.class))
      .getDialect()
      .getClass()
      .getName()
      .toLowerCase()
      .contains("postgres");
  }

  protected IntegrationTestFixtureContext context() {
    if (context == null) {
      context = newContext(getCallerClassName());
    }
    return context;
  }

  protected void authenticate(User user) {
    AuthenticatedUser authenticatedUser = new MvpUserDetails(
      user,
      tokenFactory.newToken()
    );
    tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    Authentication authentication = new AuthenticationToken(authenticatedUser.getToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);

  }

  protected RestClient restClient() {
    if (restClient == null) {
      restClient = new RestClient(serverPort);
    }
    return restClient;
  }

  protected RestClient asSuperAdmin() {
    return restAsUser(ELVACO_SUPER_ADMIN_USER);
  }

  protected RestClient asOtherUser() {
    return restAsUser(OTHER_USER);
  }

  protected RestClient asTestUser() {
    return restAsUser(context().user);
  }

  protected RestClient asTestAdmin() {
    return restAsUser(context().admin);
  }

  protected RestClient asTestSuperAdmin() {
    return restAsUser(context().superAdmin);
  }

  protected RestClient as(User user) {
    return restAsUser(user);
  }

  protected User createUserIfNotPresent(User user) {
    return users
      .findByEmail(user.email)
      .orElseGet(() -> users.create(user));
  }

  protected RestClient restAsUser(User user) {
    createUserIfNotPresent(user);
    return restClient()
      .loginWith(user.getUsername(), user.password)
      .tokenAuthorization();
  }

  /* Use with caution. Is there *any* way for you to check your assertion besides continuous
  polling? Then do that instead! */
  protected boolean waitForCondition(BooleanSupplier cond) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    do {
      if (cond.getAsBoolean()) {
        return true;
      }
      Thread.sleep(100);
    } while (System.currentTimeMillis() < startTime + MAX_WAIT_TIME);
    return false;
  }

  private String getCallerClassName() {
    return Thread.currentThread().getStackTrace()[3].getClassName();
  }

  private IntegrationTestFixtureContext newContext(String identifier) {
    return getIntegrationTestFixtureContextFactory().create(identifier);
  }

  private IntegrationTestFixtureContextFactory getIntegrationTestFixtureContextFactory() {
    if (integrationTestFixtureContextFactory == null) {
      integrationTestFixtureContextFactory = new IntegrationTestFixtureContextFactory(
        organisationJpaRepository,
        users
      );
    }
    return integrationTestFixtureContextFactory;
  }
}
