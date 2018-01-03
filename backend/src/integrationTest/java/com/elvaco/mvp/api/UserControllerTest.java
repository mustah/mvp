package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.dto.UnauthorizedDto;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends IntegrationTest {

  @Test
  public void findAllUsers() {
    List users = restClient()
      .loginWith("user", "password")
      .get("/users", List.class)
      .getBody();

    assertThat(users.size()).isGreaterThanOrEqualTo(3);
  }

  @Test
  public void findUserById() {
    UserDto user = restClient()
      .loginWith("user", "password")
      .get("/users/1", UserDto.class)
      .getBody();

    assertThat(user.id).isEqualTo(1);
  }

  @Test
  public void unableToFindNoneExistingUser() {
    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("user", "password")
      .get("/users/-999", UnauthorizedDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void userIsNotFullyAuthorized() {
    String path = "/users/2";

    UnauthorizedDto errorMessage = restClient()
      .logout()
      .get(path, UnauthorizedDto.class)
      .getBody();

    assertThat(errorMessage.message)
      .isEqualTo("Full authentication is required to access this resource");
    assertThat(errorMessage.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(errorMessage.error).isEqualTo("Unauthorized");
    assertThat(errorMessage.path).isEqualTo("/api" + path);
    assertThat(errorMessage.timestamp).isNotNull();
  }

  @Test
  public void userWithBadCredentials() {
    String path = "/users/3";

    UnauthorizedDto errorMessage = restClient()
      .loginWith("admin", "wrong-password")
      .get(path, UnauthorizedDto.class)
      .getBody();

    assertThat(errorMessage.message).isEqualTo("Bad credentials");
    assertThat(errorMessage.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(errorMessage.error).isEqualTo("Unauthorized");
    assertThat(errorMessage.path).isEqualTo("/api" + path);
    assertThat(errorMessage.timestamp).isNotNull();
  }
}
