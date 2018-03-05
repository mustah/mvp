package com.elvaco.mvp.core.usecase;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testing.cache.MockTokenService;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.UserTestData.MARVEL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UserUseCasesTest {

  private UserUseCases useCases;
  private TokenService tokenService;

  @Test
  public void adminCanUpdateUserWithinSameOrganisation() {
    User user = new User(
      randomUUID(),
      "t1",
      "t2@email.com",
      newPassword(),
      DAILY_PLANET,
      asList(USER, ADMIN)
    );

    User userToUpdate = new User(
      randomUUID(),
      "t3",
      "t3@email.com",
      newPassword(),
      DAILY_PLANET,
      singletonList(USER)
    );
    usersOf(currentUser(user), user, userToUpdate);

    String token = randomUUID().toString();
    tokenService.saveToken(token, new MockAuthenticatedUser(userToUpdate, token));

    Optional<User> update = useCases.update(userToUpdate);

    assertThat(update.get().getUsername()).isEqualTo("t3@email.com");
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
  }

  @Test
  public void cannotUpdateUserOutsideTheOrganisation() {
    User dailyPlanetUser = new User(
      randomUUID(),
      "t1",
      "t2@email.com",
      newPassword(),
      DAILY_PLANET,
      singletonList(USER)
    );
    User marvelUser = new User(
      randomUUID(),
      "Random Use",
      "randomUser@email.com",
      newPassword(),
      MARVEL,
      singletonList(USER)
    );
    usersOf(currentUser(marvelUser), dailyPlanetUser, marvelUser);

    assertThat(useCases.update(dailyPlanetUser).isPresent()).isFalse();
  }

  @Test
  public void superAdminCanDeleteAnyUser() {
    User superAdmin = new User(
      randomUUID(),
      "t1",
      "t2@email.com",
      newPassword(),
      DAILY_PLANET,
      asList(USER, SUPER_ADMIN)
    );
    User marvelUser = new User(
      randomUUID(),
      "Random Use",
      "randomUser@email.com",
      newPassword(),
      MARVEL,
      singletonList(USER)
    );
    usersOf(currentUser(superAdmin), superAdmin, marvelUser);

    String token = randomUUID().toString();
    tokenService.saveToken(token, new MockAuthenticatedUser(marvelUser, token));

    useCases.delete(marvelUser);

    assertThat(useCases.findById(marvelUser.id).isPresent()).isFalse();
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
  }

  private void usersOf(AuthenticatedUser currentUser, User... users) {
    tokenService = new MockTokenService();
    Users usersRepository = new MockUsers(asList(users));
    useCases = new UserUseCases(
      currentUser,
      usersRepository,
      new OrganisationPermissions(usersRepository),
      tokenService
    );
  }

  private AuthenticatedUser currentUser(User user) {
    return new MockAuthenticatedUser(user, newToken());
  }

  private static String newToken() {
    return randomUUID().toString();
  }

  private static String newPassword() {
    return randomUUID().toString();
  }
}
