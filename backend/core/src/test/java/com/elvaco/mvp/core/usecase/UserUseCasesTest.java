package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.UserPermissions;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testing.cache.MockTokenService;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UserUseCasesTest {

  private UserUseCases userUseCases;
  private TokenService tokenService;

  @Before
  public void setUp() {
    tokenService = new MockTokenService();
  }

  @Test
  public void adminCanUpdateUserWithinSameOrganisation() {
    User user = userBuilder()
      .name("t1")
      .email("t2@email.com")
      .roles(MVP_USER, MVP_ADMIN)
      .build();

    User userToUpdate = userBuilder().build();
    userUseCasesOf(currentUser(user), user, userToUpdate);

    String token = randomUUID().toString();
    tokenService.saveToken(new MockAuthenticatedUser(userToUpdate, token));

    User updatedUser = userUseCases.update(userToUpdate).get();

    assertThat(updatedUser.getUsername()).isEqualTo("random@random.com");
    assertThat(tokenService.getToken(token)).isNotPresent();
  }

  @Test
  public void cannotUpdateUserOutsideTheOrganisation() {
    User dailyPlanetUser = userBuilder()
      .name("t1")
      .email("t2@email.com")
      .build();

    User marvelUser = userBuilder()
      .organisation(MARVEL)
      .build();
    userUseCasesOf(currentUser(marvelUser), dailyPlanetUser, marvelUser);

    assertThat(userUseCases.update(dailyPlanetUser)).isNotPresent();
  }

  @Test
  public void superAdminCanDeleteAnyUser() {
    User superAdmin = userBuilder()
      .name("t1")
      .email("t2@email.com")
      .roles(MVP_USER, SUPER_ADMIN)
      .build();
    User marvelUser = userBuilder().build();
    userUseCasesOf(currentUser(superAdmin), superAdmin, marvelUser);

    String token = randomUUID().toString();
    tokenService.saveToken(new MockAuthenticatedUser(marvelUser, token));

    userUseCases.delete(marvelUser.id);

    assertThat(userUseCases.findById(marvelUser.id)).isNotPresent();
    assertThat(tokenService.getToken(token)).isNotPresent();
  }

  private void userUseCasesOf(AuthenticatedUser currentUser, User... users) {
    Users usersRepository = new MockUsers(asList(users));
    userUseCases = new UserUseCases(
      currentUser,
      usersRepository,
      new UserPermissions(usersRepository),
      tokenService,
      new MockOrganisations()
    );
  }

  private AuthenticatedUser currentUser(User user) {
    return new MockAuthenticatedUser(user, randomUUID().toString());
  }

  private static UserBuilder userBuilder() {
    return new UserBuilder()
      .name("Random User")
      .email("random@random.com")
      .password(randomUUID().toString())
      .organisation(DAILY_PLANET)
      .asMvpUser();
  }
}
