package com.elvaco.mvp.testdata;

import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.elvaco.mvp.fixture.DomainModels.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.fixture.DomainModels.ELVACO_USER;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// By default, remove application context after each class to avoid
// contamination of unrelated tests
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTest {

  @LocalServerPort
  private int serverPort;

  private RestClient restClient;

  public RestClient restClient() {
    if (restClient == null) {
      restClient = new RestClient(serverPort);
    }
    return restClient;
  }

  protected RestClient asElvacoUser() {
    return restClient().loginWith(ELVACO_USER.email, ELVACO_USER.password);
  }

  protected RestClient asAdminOfElvaco() {
    return restClient().loginWith(ELVACO_ADMIN_USER.email, ELVACO_ADMIN_USER.password);
  }

  protected RestClient asSuperAdmin() {
    return restClient().loginWith(ELVACO_SUPER_ADMIN_USER.email, ELVACO_SUPER_ADMIN_USER.password);
  }
}