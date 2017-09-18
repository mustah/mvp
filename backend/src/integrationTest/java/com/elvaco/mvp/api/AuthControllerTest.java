package com.elvaco.mvp.api;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.elvaco.mvp.dto.ErrorMessageDTO;
import com.elvaco.mvp.dto.UserDTO;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.UserRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import static com.elvaco.mvp.testdata.RestClient.restClient;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void authenticate() {
    String email = "evanil@elvaco.se";

    UserDTO user = restClient()
      .loginWith(email, "eva123")
      .get("/authenticate", UserDTO.class)
      .getBody();

    UserEntity expected = userRepository.findByEmail(email).get();
    assertThat(user.id).isEqualTo(expected.id);
    assertThat(user.email).isEqualTo(expected.email);
    assertThat(user.company).isEqualTo(expected.company);
  }

  @Test
  public void userIsNotAuthenticated() {
    ErrorMessageDTO user = restClient()
      .get("/authenticate", ErrorMessageDTO.class)
      .getBody();

    assertThat(user.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(user.message).isEqualTo("Full authentication is required to access this resource");
  }

  @Test
  public void userWithBadCredentials() {
    ErrorMessageDTO user = restClient()
      .loginWith("abc@d.com", "123123")
      .get("/authenticate", ErrorMessageDTO.class)
      .getBody();

    assertThat(user.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(user.message).isEqualTo("Bad credentials");
  }
}
