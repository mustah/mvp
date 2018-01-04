package com.elvaco.mvp.api;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.dto.ErrorMessageDto;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void authenticate() {
    String email = "evanil@elvaco.se";

    ResponseEntity<UserDto> response = restClient()
      .loginWith(email, "eva123")
      .get("/authenticate", UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().email).isEqualTo("evanil@elvaco.se");
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
