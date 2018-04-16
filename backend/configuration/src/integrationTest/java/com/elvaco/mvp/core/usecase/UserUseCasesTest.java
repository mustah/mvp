package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UserUseCasesTest extends IntegrationTest {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private TokenFactory tokenFactory;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserUseCases userUseCases;

  @Test
  public void newlyCreatedUserShouldHaveEncodedPassword() {
    authenticateSuperAdminUser();

    String rawPassword = "test123";

    User user = userUseCases.create(newUser(rawPassword, "testing123@b.com")).get();

    assertThat(user.password).isNotEqualTo(rawPassword);
    assertThat(passwordEncoder.matches(rawPassword, user.password)).isTrue();
  }

  @Test
  public void updateUserShouldNotUpdatePassword() {
    authenticateSuperAdminUser();

    String rawPassword = "test123";

    User user = userUseCases.create(newUser(rawPassword, "me@me.com")).get();

    User userToUpdate = UserBuilder.from(user)
      .name("Clark Kent")
      .password("passwordShouldNotBeUsed")
      .build();

    User updateUser = userUseCases.update(userToUpdate).get();

    assertThat(updateUser.name).isEqualTo("Clark Kent");
    assertThat(updateUser.password).isEqualTo(user.password);
    assertThat(passwordEncoder.matches(rawPassword, user.password)).isTrue();
  }

  @Test
  public void removeUserTokenForOtherUpdatedUser() {
    authenticateSuperAdminUser();

    User user = newUser(randomUUID().toString(), "abc@123.com");
    AuthenticatedUser authenticatedUser = userUseCases.create(user)
      .map(this::saveUserToTokenService)
      .orElseThrow(IllegalStateException::new);

    userUseCases.update(user.withName("Coder Loader"));

    assertThat(tokenService.getToken(authenticatedUser.getToken()).isPresent()).isFalse();
  }

  @Test
  public void updateLanguageForUser() {
    authenticateSuperAdminUser();

    User user = userUseCases.create(newUser("password", "test@me.com")).get();

    User updatedUser = UserBuilder.from(user)
      .name("Clark Kent")
      .language(Language.sv)
      .build();

    User confirmedUpdatedUser = userUseCases.update(updatedUser).get();

    assertThat(user.language).isEqualTo(Language.en);
    assertThat(updatedUser.language).isEqualTo(Language.sv);
    assertThat(confirmedUpdatedUser.language).isEqualTo(Language.sv);
  }

  private AuthenticatedUser saveUserToTokenService(User user) {
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(user, randomUUID().toString());
    tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    return authenticatedUser;
  }

  private User newUser(String rawPassword, String email) {
    return new UserBuilder()
      .name("john doh")
      .email(email)
      .password(rawPassword)
      .language(Language.en)
      .organisationElvaco()
      .roles(ADMIN, USER)
      .build();
  }

  private void authenticateSuperAdminUser() {
    AuthenticatedUser authenticatedUser = new MvpUserDetails(
      new UserBuilder()
        .name("Integration test user")
        .email("noone@nowhere")
        .password("nopass")
        .organisationElvaco()
        .asSuperAdmin()
        .build(),
      tokenFactory.newToken()
    );
    tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    Authentication authentication = new AuthenticationToken(authenticatedUser.getToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
