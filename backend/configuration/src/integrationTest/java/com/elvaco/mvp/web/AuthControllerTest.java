package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserTokenDto;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @Test
  public void authenticate() {
    User user = createUserIfNotPresent(ELVACO_SUPER_ADMIN_USER);

    ResponseEntity<UserTokenDto> response = restClient()
      .loginWith(user.email, user.password)
      .get("/authenticate", UserTokenDto.class);

    UserTokenDto body = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(body.user.email).isEqualTo(user.email);
    assertThat(body.token).isNotNull();
  }

  @Test
  public void unAuthorized() {
    ResponseEntity<UserDto> response = restClient()
      .loginWith("nothing", "nothing")
      .tokenAuthorization()
      .get("/users/1", UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
