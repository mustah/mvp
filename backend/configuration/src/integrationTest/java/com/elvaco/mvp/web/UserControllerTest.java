package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.UnauthorizedDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.mapper.OrganisationMapper;
import com.elvaco.mvp.web.mapper.UserMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.fixture.DomainModels.OTHER_ELVACO_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.WAYNE_INDUSTRIES;
import static com.elvaco.mvp.testdata.RestClient.apiPathOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends IntegrationTest {

  @Autowired
  private Users users;

  @Autowired
  private UserMapper userMapper;

  private final OrganisationMapper organisationMapper = new OrganisationMapper();

  @Test
  public void findUserById() {
    ResponseEntity<UserDto> response = asSuperAdmin()
      .get("/users/1", UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(1);
  }

  @Test
  public void unableToFindNoneExistingUser() {
    ResponseEntity<UserDto> response = asSuperAdmin()
      .get("/users/-999", UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
    expected.path = apiPathOf(path);

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
    expected.path = apiPathOf(path);

    UnauthorizedDto error = response.getBody();
    expected.timestamp = error.timestamp;

    assertThat(error).isEqualTo(expected);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void findAllUsers() {
    ResponseEntity<List> response = asSuperAdmin()
      .get("/users", List.class);

    assertThat(response.getBody().size()).isGreaterThanOrEqualTo(3);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void createNewUser() {
    UserWithPasswordDto user = createUserDto("n@b.com", "someNewPassword");

    ResponseEntity<UserDto> response = asSuperAdmin()
      .post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void updateSavedUserName() {
    String newName = "Eva Andersson";

    User otherElvacoUser = users.create(OTHER_ELVACO_USER);
    UserDto userDto = userMapper.toDto(otherElvacoUser);
    assertThat(userDto.name).isNotEqualTo(newName);
    userDto.name = newName;

    asSuperAdmin().put("/users", userDto);

    User updatedUser = users.findById(otherElvacoUser.id).get();

    assertThat(updatedUser.name).isEqualTo(newName);
  }

  @Test
  public void deleteUserWithId() {
    User user = users.create(new User(
      "john doh",
      "noo@b.com",
      "test123",
      ELVACO,
      asList(ADMIN, USER)
    ));

    ResponseEntity<UserDto> response = asSuperAdmin()
      .delete("/users/" + user.id, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(user.id);
  }

  @Test
  public void regularUserCannotCreateUser() {
    UserWithPasswordDto user = createUserDto("simple@user.com", "test123");

    ResponseEntity<UnauthorizedDto> response = asElvacoUser().post(
      "/users",
      user,
      UnauthorizedDto.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotDeleteUser() {
    User regularUser = users.create(new User(
      "Someu Ser",
      "thisguy@users.net",
      "hunter2",
      ELVACO,
      singletonList(USER)
    ));
    asElvacoUser().delete("/users/" + regularUser.id);
    assertThat(users.findById(regularUser.id).isPresent()).isTrue();
  }

  @Test
  public void regularUserCannotUpdateOtherUser() {
    String email = "another@user.com";
    UserDto user = createUserDto(email);

    asElvacoUser().put("/users", user);

    assertThat(users.findByEmail(email).isPresent()).isFalse();
  }

  @Test
  public void regularUserCanOnlySeeOtherUsersWithinSameOrganisation() {
    ResponseEntity<List<UserDto>> response = asElvacoUser().getList("/users", UserDto.class);
    response.getBody().forEach(u -> assertThat(u.organisation.code)
      .isEqualTo("elvaco"));
  }

  @Test
  public void newlyCreatedUserShouldBeAbleToLogin() {
    String email = "newest@member.com";
    String password = "testing";
    UserWithPasswordDto user = createUserDto(email, password);

    HttpStatus statusCode = asSuperAdmin()
      .post("/users", user, UserDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.CREATED);

    statusCode = restClient()
      .logout()
      .loginWith(email, password)
      .tokenAuthorization()
      .get("/users", List.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void superAdminCanCreateUserOfDifferentOrganisation() {
    UserWithPasswordDto user = createUserDto("jacket@player.hm", "nana yeye");

    ResponseEntity<UserDto> response = asSuperAdmin().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void adminCanCreateUserOfSameOrganisation() {
    UserWithPasswordDto user = createUserDto("stranger@danger.us", "hello");

    ResponseEntity<UserDto> response = asAdminOfElvaco().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void adminCannotCreateUserOfDifferentOrganisation() {
    UserDto user = createUserDto("50@blessings.hm", WAYNE_INDUSTRIES);

    ResponseEntity<UserDto> response = asAdminOfElvaco().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void adminCannotSeeUsersOfDifferentOrganisation() {
    UserDto batman = asSuperAdmin()
      .post("/users", createUserDto("b@tm.an", WAYNE_INDUSTRIES), UserDto.class)
      .getBody();

    UserDto colleague = asSuperAdmin()
      .post("/users", createUserDto("my.colleague@elvaco.se"), UserDto.class)
      .getBody();

    ResponseEntity<List<UserDto>> responseList = asAdminOfElvaco().getList("/users", UserDto.class);
    assertThat(responseList.getBody()).doesNotContain(batman);
    assertThat(responseList.getBody()).contains(colleague);
  }

  private UserWithPasswordDto createUserDto(String email) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Ninja Code";
    user.email = email;
    user.password = "secret stuff";
    user.organisation = organisationMapper.toDto(ELVACO);
    user.roles = asList(USER.role, ADMIN.role, SUPER_ADMIN.role);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, String password) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = password;
    user.organisation = organisationMapper.toDto(ELVACO);
    user.roles = asList(USER.role, ADMIN.role);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, Organisation organisation) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = "i am batman";
    user.organisation = organisationMapper.toDto(organisation);
    user.roles = singletonList(USER.role);
    return user;
  }
}
