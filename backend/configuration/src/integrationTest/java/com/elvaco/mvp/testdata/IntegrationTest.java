package com.elvaco.mvp.testdata;

import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

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
    return restClient().loginWith("peteri@elvaco.se", "peter123");
  }

  protected RestClient asAdminOfElvaco() {
    return restClient().loginWith("hansjo@elvaco.se", "hanna123");
  }

  protected RestClient asSuperAdmin() {
    return restClient().loginWith("user@domain.tld", "complicated_password");
  }
}
