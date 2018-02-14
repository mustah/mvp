package com.elvaco.mvp.testdata;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_USER;
import static java.util.Collections.singletonList;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// By default, remove application context after each class to avoid
// contamination of unrelated tests
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTest {

  @Autowired
  private Users users;

  @LocalServerPort
  private int serverPort;
  private RestClient restClient;

  @After
  public final void tearDownBase() {
    restClient().logout();
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Before
  public final void setUpBase() {
    MvpUserDetails principal = new MvpUserDetails(
      new User(
        "Integration test user",
        "noone@nowhere",
        "nopass",
        ELVACO,
        singletonList(Role.SUPER_ADMIN)
      )
    );
    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
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

  private RestClient restAsUser(User user) {
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
    return restClient().loginWith(user.email, user.password);
  }
}
