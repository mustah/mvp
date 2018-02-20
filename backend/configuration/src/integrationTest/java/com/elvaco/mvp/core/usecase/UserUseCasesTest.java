package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserUseCasesTest extends IntegrationTest {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private UserUseCases userUseCases;

  @Autowired
  private PasswordEncoder passwordEncoder;

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

    User userToUpdate = new User(
      user.id,
      "Clark Kent",
      user.email,
      "passwordShouldNotBeUsed",
      user.organisation,
      user.roles
    );

    User updateUser = userUseCases.update(userToUpdate).get();

    assertThat(updateUser.name).isEqualTo("Clark Kent");
    assertThat(updateUser.password).isEqualTo(user.password);
    assertThat(passwordEncoder.matches(rawPassword, user.password)).isTrue();
  }

  private User newUser(String rawPassword, String email) {
    return new User(
      "john doh",
      email,
      rawPassword,
      ELVACO,
      asList(ADMIN, USER)
    );
  }

  private void authenticateSuperAdminUser() {
    AuthenticatedUser authenticatedUser = new MvpUserDetails(
      new User(
        "Integration test user",
        "noone@nowhere",
        "nopass",
        ELVACO,
        singletonList(SUPER_ADMIN)
      )
    );
    tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    Authentication authentication = new AuthenticationToken(authenticatedUser.getToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
