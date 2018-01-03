package com.elvaco.mvp.api;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.dto.ErrorMessageDto;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @Autowired
  private UserJpaRepository userRepository;

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void authenticate() {
    String email = "evanil@elvaco.se";

    UserDto user = restClient()
      .loginWith(email, "eva123")
      .get("/authenticate", UserDto.class)
      .getBody();

    UserEntity expected = userRepository.findByEmail(email).get();
    assertThat(user.id).isEqualTo(expected.id);
    assertThat(user.email).isEqualTo(expected.email);
    assertThat(user.company).isEqualTo(expected.company);
  }

  @Test
  public void userIsNotAuthenticated() {
    ErrorMessageDto user = restClient()
      .get("/authenticate", ErrorMessageDto.class)
      .getBody();

    assertThat(user.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(user.message).isEqualTo("Full authentication is required to access this resource");
  }

  @Test
  public void userWithBadCredentials() {
    ErrorMessageDto user = restClient()
      .loginWith("abc@d.com", "123123")
      .get("/authenticate", ErrorMessageDto.class)
      .getBody();

    assertThat(user.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(user.message).isEqualTo("Bad credentials");
  }
}
