package com.elvaco.mvp.bootstrap.demo;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.RoleRepository;
import com.elvaco.mvp.security.MvpUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
@Slf4j
public class UserDatabaseLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final OrganisationRepository organisationRepository;
  private final UserUseCases userUseCases;
  private final SettingUseCases settingUseCases;

  @Autowired
  public UserDatabaseLoader(
    RoleRepository roleRepository,
    OrganisationRepository organisationRepository,
    UserUseCases userUseCases,
    SettingUseCases settingUseCases
  ) {
    this.roleRepository = roleRepository;
    this.organisationRepository = organisationRepository;
    this.userUseCases = userUseCases;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) {

    if (settingUseCases.isDemoUsersLoaded()) {
      log.info("Demo users seems to already be loaded - skipping demo user loading!");
      return;
    }
    User systemUser = new User(
      "system",
      "system@elvaco.se",
      "",
      null,
      singletonList(Role.superAdmin())
    );
    Authentication authentication = new UsernamePasswordAuthenticationToken(new MvpUserDetails(
      systemUser), null);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    organisationRepository.save(asList(
      new OrganisationEntity(
        1L,
        "Elvaco",
        "elvaco"
      ),
      new OrganisationEntity(
        2L,
        "Wayne Industries",
        "wayne-industries"
      )
    ));

    roleRepository.save(asList(
      RoleEntity.user(),
      RoleEntity.admin(),
      RoleEntity.superAdmin()
    ));

    Organisation elvaco = new Organisation(1L, "Elvaco", "elvaco");
    Organisation wayneIndustries = new Organisation(2L, "Wayne Industries", "wayne-industries");

    Role user = Role.user();
    Role admin = Role.admin();
    Role superAdmin = Role.superAdmin();

    List<User> users = asList(
      new User(
        "Peter Eriksson",
        "peteri@elvaco.se",
        "peter123",
        elvaco,
        singletonList(user)
      ),
      new User(
        "Stefan Stefanson",
        "steste@elvaco.se",
        "stefan123",
        elvaco,
        singletonList(user)
      ),
      new User(
        "Emil Tirén",
        "emitir@elvaco.se",
        "emil123",
        elvaco,
        asList(user, admin, superAdmin)
      ),
      new User(
        "Hanna Sjöstedt",
        "hansjo@elvaco.se",
        "hanna123",
        elvaco,
        asList(user, admin)
      ),
      new User(
        "User Fake",
        "user@wayne.se",
        "user123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Elvis Cohan",
        "elvis.cohan@wayne.com",
        "elvis123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Anna Johansson",
        "annjoh@wayne.se",
        "anna123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Maria Svensson",
        "marsve@wayne.se",
        "maria123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Erik Karlsson",
        "erikar@wayne.se",
        "erik123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Eva Nilsson",
        "evanil@elvaco.se",
        "eva123",
        elvaco,
        asList(user, superAdmin)
      ),
      new User(
        "Super Admin",
        "superadmin@elvaco.se",
        "admin123",
        elvaco,
        singletonList(superAdmin)
      ),
      new User(
        "Developer",
        "user@domain.tld",
        "complicated_password",
        elvaco,
        singletonList(superAdmin)
      )
    );

    users.stream()
      .map(u -> u.withPassword(() -> u.password))
      .forEach(userUseCases::create);

    settingUseCases.setDemoUsersLoaded();
  }
}
