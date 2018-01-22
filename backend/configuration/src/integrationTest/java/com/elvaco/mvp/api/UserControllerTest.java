package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.dto.OrganisationDto;
import com.elvaco.mvp.dto.UnauthorizedDto;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.dto.UserWithPasswordDto;
import com.elvaco.mvp.mapper.UserMapper;
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

  @Autowired
  private UserMapper userMapper;

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
    UserWithPasswordDto user = createUserDto("n@b.com", "someNewPassword");

    ResponseEntity<UserDto> response = apiService()
      .post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void updateSavedUserName() {
    User user = users.findByEmail("evanil@elvaco.se").get();

    UserDto userDto = userMapper.toDto(user);
    userDto.name = "Eva Andersson";

    apiService().put("/users", userDto);

    User updatedUser = users.findById(user.id).get();

    assertThat(updatedUser.name).isEqualTo("Eva Andersson");
  }

  @Test
  public void deleteUserWithId() {
    User user = users.save(new User(
      "john doh",
      "noo@b.com",
      "test123",
      new Organisation(1L, "Elvaco", "elvaco"),
      asList(Role.admin(), Role.user())
    ));

    apiService().delete("/users/" + user.id);

    assertThat(users.findById(user.id).isPresent()).isFalse();
  }

  @Test
  public void onlySuperAdminCanCreateNewUser() {
    UserWithPasswordDto user = createUserDto("simple@user.com", "test123");

    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("marsve@elvaco.se", "maria123")
      .post("/users", user, UnauthorizedDto.class);

    assertForbidden(response);
  }

  @Test
  public void onlySuperAdminCanDeleteUser() {
    String email = "annjoh@elvaco.se";
    User user = users.findByEmail(email).get();

    restClient()
      .loginWith(email, "anna123")
      .delete("/users/" + user.id);

    assertThat(users.findById(user.id).isPresent()).isTrue();
  }

  @Test
  public void onlySuperAdminAndAdminCanUpdateUser() {
    String email = "another@user.com";
    UserDto user = createUserDto(email);

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

  @Test
  public void newlyCreatedUserShouldBeAbleToLogin() {
    String email = "newest@member.com";
    String password = "testing";
    UserWithPasswordDto user = createUserDto(email, password);

    HttpStatus statusCode = apiService()
      .post("/users", user, UserDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);

    statusCode = restClient()
      .logout()
      .loginWith(email, password)
      .get("/users", List.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);
  }

  private void assertForbidden(ResponseEntity<UnauthorizedDto> response) {
    UnauthorizedDto expected = new UnauthorizedDto();
    expected.message = "Access is denied";
    expected.status = HttpStatus.FORBIDDEN.value();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isEqualTo(expected);
  }

  private UserDto createUserDto(String email) {
    UserDto user = new UserDto();
    user.name = "Ninja Code";
    user.email = email;
    user.organisation = new OrganisationDto(1L, "Elvaco", "elvaco");
    user.roles = asList(Role.USER, Role.ADMIN, Role.SUPER_ADMIN);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, String password) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = password;
    user.organisation = new OrganisationDto(2L, "Wayne Industries", "wayne-industries");
    user.roles = asList(Role.USER, Role.ADMIN);
    return user;
  }

  private RestClient apiService() {
    return restClient().loginWith("user@domain.tld", "complicated_password");
  }
}
