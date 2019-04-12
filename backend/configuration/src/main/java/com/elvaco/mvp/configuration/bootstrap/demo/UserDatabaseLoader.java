package com.elvaco.mvp.configuration.bootstrap.demo;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor
@Order(2)
@Profile("demo")
@Component
@Slf4j
class UserDatabaseLoader implements CommandLineRunner {

  private static final Organisation WAYNE_INDUSTRIES = Organisation.of("Wayne Industries");

  private static final User OTHER_ADMIN_USER = new User(
    "Elvis Cohan",
    "elvis.cohan@wayne.com",
    "elvis123",
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(ADMIN)
  );
  private static final User OTHER_USER = new User(
    "Erik Karlsson",
    "erikar@wayne.se",
    "erik123",
    Language.en,
    WAYNE_INDUSTRIES,
    singletonList(USER)
  );
  private final TokenService tokenService;
  private final TokenFactory tokenFactory;
  private final Organisations organisations;
  private final UserUseCases userUseCases;
  private final SettingUseCases settingUseCases;
  private final Organisation rootOrganisation;

  @Override
  public void run(String... args) {
    if (settingUseCases.isDemoUsersLoaded()) {
      log.info("Demo users seems to already be loaded - skipping demo user loading!");
      return;
    }

    log.info("Loading demo users");

    organisations.save(WAYNE_INDUSTRIES);

    AuthenticatedUser authenticatedUser = new MvpUserDetails(
      new User(
        "Super Admin",
        "superadmin@elvaco.se",
        "admin123",
        Language.en,
        rootOrganisation,
        singletonList(SUPER_ADMIN)
      ),
      tokenFactory.newToken()
    );

    tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    Authentication authentication = new AuthenticationToken(
      authenticatedUser.getToken(),
      authenticatedUser
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    List<User> users = asList(
      new User(
        "Emil Tirén",
        "emitir@elvaco.se",
        "emil123",
        Language.sv,
        rootOrganisation,
        asList(USER, ADMIN, SUPER_ADMIN)
      ),
      new User(
        "Hanna Sjöstedt",
        "hansjo@elvaco.se",
        "hanna123",
        Language.en,
        rootOrganisation,
        asList(USER, ADMIN)
      ),
      new User(
        "User Fake",
        "user@wayne.se",
        "user123",
        Language.sv,
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),

      new User(
        "Anna Johansson",
        "annjoh@wayne.se",
        "anna123",
        Language.sv,
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),
      new User(
        "Maria Svensson",
        "marsve@wayne.se",
        "maria123",
        Language.sv,
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),
      OTHER_ADMIN_USER,
      OTHER_USER,
      new User(
        "Stefan Stefanson",
        "steste@elvaco.se",
        "stefan123",
        Language.en,
        rootOrganisation,
        singletonList(USER)
      ),
      new User(
        "Eva Nilsson",
        "evanil@elvaco.se",
        "eva123",
        Language.en,
        rootOrganisation,
        singletonList(USER)
      ),
      new User(
        "Peter Eriksson",
        "peteri@elvaco.se",
        "peter123",
        Language.en,
        rootOrganisation,
        singletonList(ADMIN)
      ),
      new User(
        "Super Admin",
        "superadmin@elvaco.se",
        "admin123",
        Language.en,
        rootOrganisation,
        singletonList(SUPER_ADMIN)
      ),
      new User(
        "Developer",
        "user@domain.tld",
        "complicated_password",
        Language.en,
        rootOrganisation,
        singletonList(SUPER_ADMIN)
      )
    );

    users.stream()
      .map(u -> u.withPassword(u.password))
      .forEach(userUseCases::create);

    settingUseCases.setDemoUsersLoaded();
    SecurityContextHolder.clearContext();
  }
}
