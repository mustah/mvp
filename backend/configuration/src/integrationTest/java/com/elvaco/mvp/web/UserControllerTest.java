package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.PasswordDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;
import com.elvaco.mvp.web.mapper.UserDtoMapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.testdata.RestClient.apiPathOf;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends IntegrationTest {

  @Autowired
  private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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

  /**
   * Spring's DefaultErrorAttribute class builds this
   * DTO as a map, but it is not used explicitly in our production code.
   * <p/>
   * It is important not to change the representation of this map, since that would break the
   * frontend's expectations.
   * <p/>
   * Note that this DTO is a special case, compared to other "error DTO's", since it is not produced
   * in the ApiExceptionHandler, but rather by Spring Security.
   *
   * @see com.elvaco.mvp.web.api.ApiExceptionHandler
   * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes
   **/
  @Test
  public void userIsNotAuthorized() {
    String path = "/users/2";

    ResponseEntity<UnauthorizedDto> response = restClient()
      .get(path, UnauthorizedDto.class);

    UnauthorizedDto error = response.getBody();

    UnauthorizedDto expected = UnauthorizedDto.builder()
      .message("Full authentication is required to access this resource")
      .timestamp(error.timestamp)
      .path(apiPathOf(path)).build();

    assertThat(error).isEqualTo(expected);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void userWithBadCredentials() {
    String path = "/users/3";

    ResponseEntity<UnauthorizedDto> response = restClient()
      .loginWith("admin", "wrong-password")
      .get(path, UnauthorizedDto.class);

    UnauthorizedDto error = response.getBody();
    UnauthorizedDto expected = UnauthorizedDto.builder()
      .message("Bad credentials")
      .timestamp(error.timestamp)
      .path(apiPathOf(path)).build();

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

    User user = users.save(
      new UserBuilder()
        .name("First Name")
        .email("t@b.com")
        .password("ttt123")
        .language(Language.en)
        .organisation(context().defaultOrganisation())
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
    User user = users.save(
      userBuilder()
        .email("noo@b.com")
        .password("test123")
        .organisation(context().defaultOrganisation())
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
    User user = users.save(
      userBuilder()
        .name("Someu Ser")
        .email("thisguy@users.net")
        .password("hunter2")
        .organisation(context().defaultOrganisation())
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
  public void regularUserCannotElevateOwnRole() {
    String password = "ttt123";
    User user = users.save(
      new UserBuilder()
        .name("First Name")
        .email("t@b.com")
        .password(password)
        .language(Language.en)
        .organisation(context().defaultOrganisation())
        .asUser()
        .build()
    ).withPassword(password);

    UserDto userDto = UserDtoMapper.toDto(user);
    userDto.roles = List.of(ADMIN.role);

    ResponseEntity<UserDto> response = as(user).put("/users", userDto, UserDto.class);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findById(user.id).get().roles).containsExactly(USER);

    userDto.roles = List.of(SUPER_ADMIN.role);

    response = as(user).put("/users", userDto, UserDto.class);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findById(user.id).get().roles).containsExactly(USER);
  }

  @Test
  public void regularUserCanOnlySeeOtherUsersWithinSameOrganisation() {
    ResponseEntity<List<UserDto>> response = asUser().getList("/users", UserDto.class);

    List<String> organisationCodes = response.getBody().stream()
      .map(u -> u.organisation.slug)
      .collect(toList());

    assertThat(organisationCodes).isNotEmpty();
    assertThat(organisationCodes).containsOnly(context().defaultOrganisation().slug);
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
  public void adminCanCreateUserInSubOrganisation() throws IOException {
    createUserIfNotPresent(context().admin);

    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .organisationId(context().admin.organisation.id)
      .name("")
      .selectionParameters(OBJECT_MAPPER.readTree("{\"test\":\"test selection\"}"))
      .ownerUserId(context().admin.id)
      .build();

    selection = userSelections.save(selection);

    Organisation organisation = Organisation.builderFrom("Sub organisation3")
      .parent(context().defaultOrganisation())
      .selection(selection)
      .build();

    organisations.save(organisation);

    UserWithPasswordDto user = createUserDto("superman@dailyplanet.us", organisation);

    ResponseEntity<UserDto> response = asAdmin().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isNotNull();
    assertThat(savedUser.name).isEqualTo(user.name);
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
    UserDto user = createUserDto("50@blessings.hm", given(organisation()));

    ResponseEntity<UserDto> response = asAdmin().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void adminCannotSeeUsersOfDifferentOrganisation() {
    UserDto batman = asSuperAdmin()
      .post("/users", createUserDto("b@tm.an", given(organisation())), UserDto.class)
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
  public void adminCannotElevateOwnRole() {
    String password = "ttt123";
    User user = users.save(
      new UserBuilder()
        .name("First Name")
        .email("t@b.com")
        .password(password)
        .language(Language.en)
        .organisation(context().defaultOrganisation())
        .asAdmin()
        .build()
    ).withPassword(password);

    UserDto userDto = UserDtoMapper.toDto(user);
    userDto.roles = List.of(SUPER_ADMIN.role);

    ResponseEntity<UserDto> response = as(user).put("/users", userDto, UserDto.class);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findById(user.id).get().roles).containsExactly(ADMIN);
  }

  @Test
  public void adminCannotCreateSuperUser() {
    String email = "ca@aaa.se";
    UserDto userDto = createUserDto(email);
    userDto.roles = List.of(SUPER_ADMIN.role);

    ResponseEntity<UserDto> response = asAdmin().put("/users", userDto, UserDto.class);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findByEmail(email).isPresent()).isFalse();
  }

  @Test
  public void adminCannotEditSuperUser() {
    String oldEmail = "aasssshjsh@a.se";

    User user = users.save(
      new UserBuilder()
        .name("First Name")
        .email(oldEmail)
        .password("asdf")
        .language(Language.en)
        .organisation(context().defaultOrganisation())
        .asSuperAdmin()
        .build()
    );

    UserDto userDto = UserDtoMapper.toDto(user);
    userDto.email = "new@email.com";

    ResponseEntity<UserDto> response = asAdmin().put("/users", userDto, UserDto.class);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findById(user.id).get().email).isEqualTo(oldEmail);
  }

  @Test
  public void adminCannotDeleteSuperUser() {
    String oldEmail = "aasssshjsh@a.se";

    User user = users.save(
      new UserBuilder()
        .name("First Name")
        .email(oldEmail)
        .password("asdf")
        .language(Language.en)
        .organisation(context().defaultOrganisation())
        .asSuperAdmin()
        .build()
    );

    UserDto userDto = UserDtoMapper.toDto(user);
    userDto.email = "new@email.com";

    ResponseEntity<UserDto> response = asAdmin().delete("/users/" + user.id, UserDto.class);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(users.findById(user.id).isPresent()).isTrue();
  }

  @Test
  public void invalidateUserWhenSuperAdminUpdatedThatUsersCredentials() {
    UserWithPasswordDto userWithPassword = createUserDto(
      "batman@batty.com",
      given(organisation())
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

  @Test
  public void superAdminCanChangeAnyUsersPassword() {
    UserWithPasswordDto userDto = createUserDto("First-Name@company.se", "ttt123");
    userDto.id = users.save(UserDtoMapper.toDomainModel(userDto)).id;

    String newPassword = "asdf";
    userDto.password = newPassword;

    asSuperAdmin().put("/users/change-password/" + userDto.id, userDto);

    User updatedUser = users.findById(userDto.id).get();

    assertThat(passwordEncoder.matches(newPassword, updatedUser.password)).isTrue();
  }

  @Test
  public void passwordCantBeUpdatedToBlankOrWhiteSpace() {
    String oldPassword = "ttt123";
    UserWithPasswordDto userDto = createUserDto("First-Name@company.se", oldPassword);
    userDto.id = users.save(UserDtoMapper.toDomainModel(userDto)).id;

    userDto.password = "";
    asSuperAdmin().put("/users", userDto);

    User updatedUser = users.findById(userDto.id).get();

    assertThat(passwordEncoder.matches(oldPassword, updatedUser.password)).isTrue();

    userDto.password = "   ";
    asSuperAdmin().put("/users/change-password/" + userDto.id, userDto);

    updatedUser = users.findById(userDto.id).get();

    assertThat(passwordEncoder.matches(oldPassword, updatedUser.password)).isTrue();
  }

  @Test
  public void userCantChangeOtherUsersPassword() {
    String oldPassword = "ttt123";
    UserWithPasswordDto userDto = createUserDto("First-Name@company.se", oldPassword);
    userDto.id = users.save(UserDtoMapper.toDomainModel(userDto)).id;

    userDto.password = "test";
    asUser().put("/users/change-password/" + userDto.id, userDto);

    User updatedUser = users.findById(userDto.id).get();

    assertThat(passwordEncoder.matches(oldPassword, updatedUser.password)).isTrue();
  }

  @Test
  public void changingOwnPasswordReturnNewToken() {
    String oldPassword = "test";
    String newPassword = "aaa";

    User user = createUserIfNotPresent(new User(
      "test",
      "someuser@elvaco.se",
      oldPassword,
      Language.en,
      context().defaultOrganisation(),
      List.of(SUPER_ADMIN)
    ));

    ResponseEntity<UserTokenDto> response =
      restClient()
        .loginWith(user.getUsername(), oldPassword)
        .get("/authenticate", UserTokenDto.class);

    String oldToken = response.getBody().token;
    assertThat(oldToken).isNotNull();

    response = restClient()
      .loginWith(user.getUsername(), oldPassword)
      .put("/users/change-password/" + user.id, new PasswordDto(newPassword), UserTokenDto.class);

    UserTokenDto body = response.getBody();

    assertThat(body.token).isNotEqualTo(oldToken);
    assertThat(body.token).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(body.user.email).isEqualTo(user.email);
    assertThat(body.token).isNotNull();
  }

  private UserWithPasswordDto createUserDto(String email) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Ninja Code";
    user.email = email;
    user.password = "secret stuff";
    user.language = Language.en;
    user.organisation = OrganisationDtoMapper.toDto(context().defaultOrganisation());
    user.roles = asList(USER.role, ADMIN.role, SUPER_ADMIN.role);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, String password) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = password;
    user.language = Language.en;
    user.organisation = OrganisationDtoMapper.toDto(context().defaultOrganisation());
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
