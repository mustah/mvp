package com.elvaco.mvp.core.usecase;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testing.cache.MockTokenService;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

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

  private UserUseCases useCases;
  private TokenService tokenService;

  @Test
  public void adminCanUpdateUserWithinSameOrganisation() {
    User user = userBuilder()
      .name("t1")
      .email("t2@email.com")
      .roles(MVP_USER, MVP_ADMIN)
      .build();

    User userToUpdate = userBuilder().build();
    usersOf(currentUser(user), user, userToUpdate);

    String token = randomUUID().toString();
    tokenService.saveToken(token, new MockAuthenticatedUser(userToUpdate, token));

    Optional<User> update = useCases.update(userToUpdate);

    assertThat(update.get().getUsername()).isEqualTo("random@random.com");
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
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
    usersOf(currentUser(marvelUser), dailyPlanetUser, marvelUser);

    assertThat(useCases.update(dailyPlanetUser).isPresent()).isFalse();
  }

  @Test
  public void superAdminCanDeleteAnyUser() {
    User superAdmin = userBuilder()
      .name("t1")
      .email("t2@email.com")
      .roles(MVP_USER, SUPER_ADMIN)
      .build();
    User marvelUser = userBuilder().build();
    usersOf(currentUser(superAdmin), superAdmin, marvelUser);

    String token = randomUUID().toString();
    tokenService.saveToken(token, new MockAuthenticatedUser(marvelUser, token));

    useCases.delete(marvelUser.id);

    assertThat(useCases.findById(marvelUser.id).isPresent()).isFalse();
    assertThat(tokenService.getToken(token).isPresent()).isFalse();
  }

  private void usersOf(AuthenticatedUser currentUser, User... users) {
    tokenService = new MockTokenService();
    Users usersRepository = new MockUsers(asList(users));
    Organisations organisations = new MockOrganisations();
    useCases = new UserUseCases(
      currentUser,
      usersRepository,
      new OrganisationPermissions(usersRepository),
      tokenService,
      organisations
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
