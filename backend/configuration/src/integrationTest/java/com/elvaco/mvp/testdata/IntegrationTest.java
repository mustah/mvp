package com.elvaco.mvp.testdata;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
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

  @Autowired
  private Users users;

  @LocalServerPort
  private int serverPort;

  private RestClient restClient;

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

  protected User createUserIfNotPresent(User user) {
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
    return user;
  }

  protected RestClient restAsUser(User user) {
    createUserIfNotPresent(user);
    return restClient()
      .loginWith(user.email, user.password)
      .tokenAuthorization();
  }
}
