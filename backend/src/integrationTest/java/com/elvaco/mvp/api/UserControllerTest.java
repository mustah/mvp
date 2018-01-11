package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.core.Roles;
import com.elvaco.mvp.core.dto.OrganisationDto;
import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.dto.UnauthorizedDto;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends IntegrationTest {

  @Autowired
  private Users users;

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void findUserById() {
    ResponseEntity<UserDto> response = apiService()
      .get("/users/4", UserDto.class);

    assertThat(response.getBody().id).isEqualTo(4);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void unableToFindNoneExistingUser() {
    ResponseEntity<UserDto> response = apiService()
      .get("/users/-999", UserDto.class);

    assertThat(response.getBody()).isNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void userIsNotAuthorized() {
    String path = "/users/2";

    ResponseEntity<UnauthorizedDto> response = restClient()
      .get(path, UnauthorizedDto.class);

    UnauthorizedDto expected = new UnauthorizedDto();
    expected.message = "Full authentication is required to access this resource";
    expected.status = HttpStatus.UNAUTHORIZED.value();
    expected.error = "Unauthorized";
    expected.path = "/api" + path;

    UnauthorizedDto error = response.getBody();
    expected.timestamp = error.timestamp;

    assertThat(error).isEqualTo(expected);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void userWithBadCredentials() {
    String path = "/users/3";

    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("admin", "wrong-password")
      .get(path, UnauthorizedDto.class);

    UnauthorizedDto expected = new UnauthorizedDto();
    expected.message = "Bad credentials";
    expected.status = HttpStatus.UNAUTHORIZED.value();
    expected.error = "Unauthorized";
    expected.path = "/api" + path;

    UnauthorizedDto error = response.getBody();
    expected.timestamp = error.timestamp;

    assertThat(error).isEqualTo(expected);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void findAllUsers() {
    ResponseEntity<List> response = apiService()
      .get("/users", List.class);

    assertThat(response.getBody().size()).isGreaterThanOrEqualTo(3);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void createNewUser() {
    UserDto user = userWithEmail("n@b.com");

    ResponseEntity<UserDto> response = apiService()
      .post("/users", user, UserDto.class);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void updateSavedUserName() {
    UserDto user = users.save(userWithEmail("anderson@neo.com"));

    user.name = "Mr. Anderson";

    apiService().put("/users", user);

    UserDto updatedUser = users.findById(user.id).get();

    assertThat(updatedUser.name).isEqualTo("Mr. Anderson");
  }

  @Test
  public void deleteUserWithId() {
    UserDto user = users.save(userWithEmail("n@b.com"));

    apiService().delete("/users/" + user.id);

    assertThat(users.findById(user.id).isPresent()).isFalse();
  }

  @Test
  public void onlySuperAdminCanCreateNewUser() {
    UserDto user = userWithEmail("simple@user.com");

    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("marsve@elvaco.se", "maria123")
      .post("/users", user, UnauthorizedDto.class);

    assertForbidden(response);
  }

  @Test
  public void onlySuperAdminCanDeleteUser() {
    restClient()
      .loginWith("marsve@elvaco.se", "maria123")
      .delete("/users/1");

    assertThat(users.findById(1L).isPresent()).isTrue();
  }

  @Test
  public void onlySuperAdminAndAdminCanUpdateUser() {
    String email = "another@user.com";
    UserDto user = userWithEmail(email);

    restClient()
      .loginWith("marsve@elvaco.se", "maria123")
      .put("/users", user);

    assertThat(users.findByEmail(email).isPresent()).isFalse();
  }

  @Test
  public void onlySuperAdminAndAdminCanSeeAllUsers() {
    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("marsve@elvaco.se", "maria123")
      .get("/users", UnauthorizedDto.class);

    assertForbidden(response);
  }

  private void assertForbidden(ResponseEntity<UnauthorizedDto> response) {
    UnauthorizedDto expected = new UnauthorizedDto();
    expected.message = "Access is denied";
    expected.status = HttpStatus.FORBIDDEN.value();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isEqualTo(expected);
  }

  private UserDto userWithEmail(String email) {
    UserDto user = new UserDto();
    user.name = "Ninja Code";
    user.email = email;
    user.organisation = new OrganisationDto(1L, "Elvaco", "elvaco");
    user.roles = asList(Roles.USER, Roles.ADMIN);
    return user;
  }

  private RestClient apiService() {
    return restClient().loginWith("user@domain.tld", "complicated_password");
  }
}
