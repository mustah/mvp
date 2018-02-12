package com.elvaco.mvp.configuration.bootstrap.demo;

import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.repository.jpa.RoleRepository;
import com.elvaco.mvp.web.security.MvpUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.core.fixture.DomainModels.DEVELOPER_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.OTHER_ADMIN_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.OTHER_ELVACO_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.OTHER_USER;
import static com.elvaco.mvp.core.fixture.DomainModels.SECRET_SERVICE;
import static com.elvaco.mvp.core.fixture.DomainModels.THE_BEATLES;
import static com.elvaco.mvp.core.fixture.DomainModels.WAYNE_INDUSTRIES;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Order(1)
@Component
@Slf4j
public class UserDatabaseLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final Organisations organisations;
  private final UserUseCases userUseCases;
  private final SettingUseCases settingUseCases;

  @Autowired
  public UserDatabaseLoader(
    RoleRepository roleRepository,
    Organisations organisations,
    UserUseCases userUseCases,
    SettingUseCases settingUseCases
  ) {
    this.roleRepository = roleRepository;
    this.organisations = organisations;
    this.userUseCases = userUseCases;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) {
    if (settingUseCases.isDemoUsersLoaded()) {
      log.info("Demo users seems to already be loaded - skipping demo user loading!");
      return;
    }

    Stream.of(
      ELVACO,
      WAYNE_INDUSTRIES,
      SECRET_SERVICE,
      THE_BEATLES
    ).forEach(organisations::save);

    MvpUserDetails principal = new MvpUserDetails(ELVACO_SUPER_ADMIN_USER);
    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    roleRepository.save(asList(
      RoleEntity.user(),
      RoleEntity.admin(),
      RoleEntity.superAdmin()
    ));

    List<User> users = asList(
      new User(
        "Emil Tirén",
        "emitir@elvaco.se",
        "emil123",
        ELVACO,
        asList(USER, ADMIN, SUPER_ADMIN)
      ),
      new User(
        "Hanna Sjöstedt",
        "hansjo@elvaco.se",
        "hanna123",
        ELVACO,
        asList(USER, ADMIN)
      ),
      new User(
        "User Fake",
        "user@wayne.se",
        "user123",
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),

      new User(
        "Anna Johansson",
        "annjoh@wayne.se",
        "anna123",
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),
      new User(
        "Maria Svensson",
        "marsve@wayne.se",
        "maria123",
        WAYNE_INDUSTRIES,
        singletonList(USER)
      ),
      OTHER_ADMIN_USER,
      OTHER_USER,
      ELVACO_USER,
      OTHER_ELVACO_USER,
      ELVACO_ADMIN_USER,
      ELVACO_SUPER_ADMIN_USER,
      DEVELOPER_USER
    );

    users.stream()
      .map(u -> u.withPassword(() -> u.password))
      .forEach(userUseCases::create);

    settingUseCases.setDemoUsersLoaded();
  }
}