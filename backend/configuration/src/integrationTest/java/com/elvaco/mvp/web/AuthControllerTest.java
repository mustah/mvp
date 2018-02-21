package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserTokenDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.RANDOM_ELVACO_USER;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest extends IntegrationTest {

  @Autowired
  private TokenService tokenService;

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

  @Test
  public void logoutUserShouldRemoveItsTokenFromTokenService() {
    User user = createUserIfNotPresent(RANDOM_ELVACO_USER);

    ResponseEntity<UserTokenDto> response = restClient()
      .loginWith(user.email, user.password)
      .get("/authenticate", UserTokenDto.class);

    String token = response.getBody().token;

    assertThat(token).isNotNull();

    ResponseEntity<Void> logoutResponse = restClient()
      .withBearerToken(token)
      .get("/logout", Void.class);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
  }

  @Test
  public void logoutUserThatIsNotInTokenServiceCache() {
    String token = UUID.randomUUID().toString();

    ResponseEntity<Void> logoutResponse = restClient()
      .withBearerToken(token)
      .get("/logout", Void.class);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
  }

  @Test
  public void logoutUserWithoutAuthenticationHeaderShouldReturnNoContentStatus() {
    ResponseEntity<Void> logoutResponse = restClient()
      .get("/logout", Void.class);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }
}
