package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

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

  @Test
  public void canUpdateUserWithinSameOrganisation() {
    User user = new User(
      randomUUID(),
      "t1",
      "t2@email.com",
      newPassword(),
      DAILY_PLANET,
      singletonList(USER)
    );

    User userToUpdate = new User(
      user.getId(),
      "test",
      "t2@email.com",
      newPassword(),
      DAILY_PLANET,
      singletonList(USER)
    );
    usersOf(authenticatedUser(user), user, userToUpdate);

    assertThat(useCases.update(userToUpdate).get().name).isEqualTo("test");
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
    usersOf(authenticatedUser(marvelUser), dailyPlanetUser, marvelUser);

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
    usersOf(authenticatedUser(superAdmin), superAdmin, marvelUser);

    useCases.delete(marvelUser);

    assertThat(useCases.findById(marvelUser.id).isPresent()).isFalse();
  }

  private void usersOf(AuthenticatedUser currentUser, User... newUsers) {
    Users users = new MockUsers(asList(newUsers));
    useCases = new UserUseCases(currentUser, users, new OrganisationPermissions(users));
  }

  private AuthenticatedUser authenticatedUser(User user) {
    return new MockAuthenticatedUser(user, newToken());
  }

  private static String newToken() {
    return randomUUID().toString();
  }

  private static String newPassword() {
    return randomUUID().toString();
  }
}
