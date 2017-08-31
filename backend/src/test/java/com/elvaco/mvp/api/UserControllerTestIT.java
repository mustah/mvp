package com.elvaco.mvp.api;

import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.elvaco.mvp.dto.UnauthorizedDTO;
import com.elvaco.mvp.entities.user.UserEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import static com.elvaco.mvp.testdata.RestClient.restClient;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTestIT extends IntegrationTest {

  @Test
  public void FindAllUsers() throws Exception {
    List users = restClient()
      .loginWith("user", "password")
      .get("/users", List.class)
      .getBody();

    assertThat(users.size()).isGreaterThanOrEqualTo(3);
  }

  @Test
  public void FindUserById() {
    UserEntity user = restClient()
      .loginWith("user", "password")
      .get("/users/1", UserEntity.class)
      .getBody();

    assertThat(user.id).isEqualTo(1);
  }

  @Test
  public void UnableToFindNoneExistingUser() {
    UserEntity user = restClient()
      .loginWith("admin", "password")
      .get("/users/-999", UserEntity.class)
      .getBody();

    assertThat(user).isNull();
  }

  @Test
  public void UserIsNotFullyAuthorized() {
    String path = "/users/1";

    UnauthorizedDTO errorMessage = restClient()
      .logout()
      .get(path, UnauthorizedDTO.class)
      .getBody();

    assertThat(errorMessage.message).isEqualTo("Full authentication is required to access this resource");
    assertThat(errorMessage.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(errorMessage.error).isEqualTo("Unauthorized");
    assertThat(errorMessage.path).isEqualTo("/api" + path);
    assertThat(errorMessage.timestamp).isNotNull();
  }

  @Test
  public void UserWithBadCredentials() {
    String path = "/users/1";

    UnauthorizedDTO errorMessage = restClient()
      .loginWith("admin", "wrong-password")
      .get(path, UnauthorizedDTO.class)
      .getBody();

    assertThat(errorMessage.message).isEqualTo("Bad credentials");
    assertThat(errorMessage.status).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(errorMessage.error).isEqualTo("Unauthorized");
    assertThat(errorMessage.path).isEqualTo("/api" + path);
    assertThat(errorMessage.timestamp).isNotNull();
  }
}
