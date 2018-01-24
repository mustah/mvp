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

import static com.elvaco.mvp.testdata.RestClient.apiPathOf;
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
    ResponseEntity<UserDto> response = asSuperAdmin()
      .get("/users/4", UserDto.class);

    assertThat(response.getBody().id).isEqualTo(4);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    asSuperAdmin().put("/users", userDto);

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

    ResponseEntity<UserDto> response = asSuperAdmin().delete(
      "/users/" + user.id, UserDto.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(user.id);
  }

  @Test
  public void regularUserCannotCreateUser() {
    UserWithPasswordDto user = createUserDto("simple@user.com", "test123");

    ResponseEntity<UnauthorizedDto> response = asUser().post("/users", user, UnauthorizedDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotDeleteUser() {
    String email = "steste@elvaco.se";
    User user = users.findByEmail(email).get();

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
    response.getBody().stream().forEach(u -> assertThat(u.organisation.code)
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

    assertThat(statusCode).isEqualTo(HttpStatus.OK);

    statusCode = restClient()
      .logout()
      .loginWith(email, password)
      .get("/users", List.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void superAdminCanCreateUserOfDifferentOrganisation() {
    UserWithPasswordDto user = createUserDto("jacket@player.hm", "nana yeye");

    ResponseEntity<UserDto> response = asSuperAdmin().post("/users", user, UserDto.class);

    // TODO replace OK with CREATED
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }


  @Test
  public void adminCanCreateUserOfSameOrganisation() {
    UserWithPasswordDto user = createUserDto("stranger@danger.us", "hello");

    ResponseEntity<UserDto> response = asAdminOfElvaco().post("/users", user, UserDto.class);

    // TODO replace OK with CREATED
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    UserDto savedUser = response.getBody();
    assertThat(savedUser.id).isPositive();
    assertThat(savedUser.name).isEqualTo(user.name);
  }

  @Test
  public void adminCannotCreateUserOfDifferentOrganisation() {
    UserDto user = createUserDto("50@blessings.hm", new OrganisationDto(50L, "Phone hom",
      "phone-hom"));

    ResponseEntity<UserDto> response = asAdminOfElvaco().post("/users", user, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }


  @Test
  public void adminCannotSeeUsersOfDifferentOrganisation() {
    UserDto batman = asSuperAdmin()
      .post("/users", createUserDto("b@tm.an", new OrganisationDto(2L, "Wayne Industries",
        "wayne-industries")), UserDto.class)
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
    user.organisation = new OrganisationDto(1L, "Elvaco", "elvaco");
    user.roles = asList(Role.USER, Role.ADMIN, Role.SUPER_ADMIN);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, String password) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = password;
    user.organisation = new OrganisationDto(1L, "Elvaco", "elvaco");
    user.roles = asList(Role.USER, Role.ADMIN);
    return user;
  }

  private UserWithPasswordDto createUserDto(String email, OrganisationDto organisation) {
    UserWithPasswordDto user = new UserWithPasswordDto();
    user.name = "Bruce Wayne";
    user.email = email;
    user.password = "i am batman";
    user.organisation = organisation;
    user.roles = asList(Role.USER);
    return user;
  }

  private RestClient asUser() {
    return restClient().loginWith("peteri@elvaco.se", "peter123");
  }

  private RestClient asAdminOfElvaco() {
    return restClient().loginWith("hansjo@elvaco.se", "hanna123");
  }

  private RestClient asSuperAdmin() {
    return restClient().loginWith("user@domain.tld", "complicated_password");
  }
}
