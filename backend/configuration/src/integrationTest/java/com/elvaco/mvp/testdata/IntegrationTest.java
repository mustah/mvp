package com.elvaco.mvp.testdata;

import java.util.function.BooleanSupplier;
import javax.persistence.EntityManagerFactory;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_USER;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

  private static final long MAX_WAIT_TIME = 2000;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private Users users;

  @Autowired
  private EntityManagerFactory factory;

  private IntegrationTestFixtureContextFactory integrationTestFixtureContextFactory;

  @LocalServerPort
  private int serverPort;


  private RestClient restClient;

  protected boolean isPostgresDialect() {
    return ((SessionFactoryImplementor) factory.unwrap(SessionFactory.class))
      .getDialect()
      .getClass()
      .getName()
      .toLowerCase()
      .contains("postgres");
  }

  protected IntegrationTestFixtureContext newContext() {
    return getIntegrationTestFixtureContextFactory().create();
  }

  protected void destroyContext(IntegrationTestFixtureContext context) {
    getIntegrationTestFixtureContextFactory().destroy(context);
  }

  @After
  public final void tearDownBase() {
    restClient().logout();
    SecurityContextHolder.clearContext();
  }

  public RestClient restClient() {
    if (restClient == null) {
      restClient = new RestClient(serverPort);
    }
    return restClient;
  }

  protected RestClient asElvacoUser() {
    return restAsUser(ELVACO_USER);
  }

  protected RestClient asAdminOfElvaco() {
    return restAsUser(ELVACO_ADMIN_USER);
  }

  protected RestClient asSuperAdmin() {
    return restAsUser(ELVACO_SUPER_ADMIN_USER);
  }

  protected RestClient as(User user) {
    return restAsUser(user);
  }

  protected User createUserIfNotPresent(User user) {
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
    return user;
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
