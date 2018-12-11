package com.elvaco.mvp.web;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;
import com.elvaco.mvp.web.mapper.UserDtoMapper;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.testdata.RestClient.apiPathOf;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends IntegrationTest {

  @Test
  public void findUserById() {
    UUID id = context().superAdmin.getId();

    ResponseEntity<UserDto> response = asSuperAdmin()
      .get("/users/" + id, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(id);
  }

  @Test
  public void unableToFindNoneExistingUser() {
    ResponseEntity<UserDto> response = asSuperAdmin()
      .get("/users/" + randomUUID(), UserDto.class);

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
    ResponseEntity<List<UserDto>> response = asSuperAdmin()
      .getList("/users", UserDto.class);

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
    assertThat(savedUser.id).isNotNull();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void updateSavedUserName() {
    String newName = "New name";

    User user = users.create(
      new UserBuilder()
        .name("First Name")
        .email("t@b.com")
        .password("ttt123")
        .language(Language.en)
        .organisation(context().organisation())
        .asUser()
        .build()
    );
    UserDto userDto = UserDtoMapper.toDto(user);
    assertThat(userDto.name).isNotEqualTo(newName);
    userDto.name = newName;

    asSuperAdmin().put("/users", userDto);

    User updatedUser = users.findById(user.id).get();

    assertThat(updatedUser.name).isEqualTo(newName);
  }

  @Test
  public void deleteUserWithId() {
    User user = users.create(
      userBuilder()
        .email("noo@b.com")
        .password("test123")
        .organisation(context().organisation())
        .build()
    );

    ResponseEntity<UserDto> response = asSuperAdmin()
      .delete("/users/" + user.id, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(user.id);
  }

  @Test
  public void regularUserCannotCreateUser() {
    UserWithPasswordDto user = createUserDto("simple@user.com", "test123");

    ResponseEntity<UnauthorizedDto> response = asUser().post(
      "/users",
      user,
      UnauthorizedDto.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotDeleteUser() {
    User user = users.create(
      userBuilder()
        .name("Someu Ser")
        .email("thisguy@users.net")
        .password("hunter2")
        .organisation(context().organisation())
        .asUser()
        .build()
    );

    asUser().delete("/users/" + user.id);

    assertThat(users.findById(user.id).isPresent()).isTrue();
  }

  @Test
  public void regularUserCannotUpdateOtherUser() {
    String email = "another@user.com";
    UserDto user = createUserDto(email);

    asUser().put("/users", user);

    assertThat(users.findByEmail(email).isPresent()).isFalse();
  }

  @Test
  public void regularUserCanOnlySeeOtherUsersWithinSameOrganisation() {
    ResponseEntity<List<UserDto>> response = asUser().getList("/users", UserDto.class);

    List<String> organisationCodes = response.getBody().stream()
      .map(u -> u.organisation.slug)
      .collect(toList());

    assertThat(organisationCodes).isNotEmpty();
    assertThat(organisationCodes).containsOnly(context().organisation().slug);
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

    UserDto savedUser = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(savedUser.id).isNotNull();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void createNewUserWithExistingEmail() {
    UserWithPasswordDto firstUser = createUserDto("first@user.com", "first user");

    ResponseEntity<UserDto> response = asSuperAdmin().post("/users", firstUser, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    UserWithPasswordDto secondUser = createUserDto("first@user.com", "second user");

    ResponseEntity<ErrorMessageDto> errorResponse = asSuperAdmin()
      .post("/users", secondUser, ErrorMessageDto.class);

    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(errorResponse.getBody().message).isEqualTo("Email address already exists");
  }

  @Test
  public void adminCanCreateUserOfSameOrganisation() {
    UserWithPasswordDto user = createUserDto("stranger@danger.us", "hello");

    ResponseEntity<UserDto> response = asAdmin().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isNotNull();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void adminCannotCreateUserOfDifferentOrganisation() {
    UserDto user = createUserDto("50@blessings.hm", context().organisation2());

    ResponseEntity<UserDto> response = asAdmin().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void adminCannotSeeUsersOfDifferentOrganisation() {
    UserDto batman = asSuperAdmin()
      .post("/users", createUserDto("b@tm.an", context().organisation2()), UserDto.class)
      .getBody();

    UserDto colleague = asSuperAdmin()
      .post("/users", createUserDto("my.colleague@elvaco.se"), UserDto.class)
      .getBody();

    ResponseEntity<List<UserDto>> responseList = asAdmin()
      .getList("/users", UserDto.class);

    assertThat(responseList.getBody()).doesNotContain(batman);
    assertThat(responseList.getBody()).contains(colleague);
  }

  @Test
  public void invalidateUserWhenSuperAdminUpdatedThatUsersCredentials() {
    UserWithPasswordDto userWithPassword = createUserDto(
      "batman@batty.com",
      context().organisation2()
    );

    ResponseEntity<UserDto> postResponse = asSuperAdmin()
      .post("/users", userWithPassword, UserDto.class);

    assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UserDto created = postResponse.getBody();
    assertThat(created.id).isNotNull();

    String token = restClient()
      .loginWith(userWithPassword.email, userWithPassword.password)
      .get("/authenticate", UserTokenDto.class)
      .getBody()
      .token;

    assertThat(token).isNotNull();

    UserDto user = new UserDto(
      created.id,
      "Wayne, Bruce",
      created.email,
      Language.en,
      created.organisation,
      created.roles
    );

    ResponseEntity<UserDto> putResponse = asSuperAdmin()
      .put("/users", user, UserDto.class);

    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(putResponse.getBody().name).isEqualTo("Wayne, Bruce");

    ResponseEntity<Unauthorized> logoutResponse = restClient()
      .withBearerToken(token)
      .get("/users/" + created.id, Unauthorized.class);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  private UserWithPasswordDto createUserDto(String email) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Ninja Code";
    user.email = email;
    user.password = "secret stuff";
    user.language = Language.en;
    user.organisation = OrganisationDtoMapper.toDto(context().organisation());
    user.roles = asList(USER.role, ADMIN.role, SUPER_ADMIN.role);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, String password) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = password;
    user.language = Language.en;
    user.organisation = OrganisationDtoMapper.toDto(context().organisation());
    user.roles = asList(USER.role, ADMIN.role);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, Organisation organisation) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = "i am batman";
    user.language = Language.en;
    user.organisation = OrganisationDtoMapper.toDto(organisation);
    user.roles = singletonList(USER.role);
    return user;
  }
}
