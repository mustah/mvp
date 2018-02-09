package com.elvaco.mvp.web;

import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UserDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.DEVELOPER_USER;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void authenticate() {
    ResponseEntity<UserDto> response = restClient()
      .loginWith(DEVELOPER_USER.email, DEVELOPER_USER.password)
      .get("/authenticate", UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().email).isEqualTo(DEVELOPER_USER.email);
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
