package com.elvaco.mvp.bootstrap.demo;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.mapper.UserDetailsMapper;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.Roles.ADMIN;
import static com.elvaco.mvp.core.Roles.SUPER_ADMIN;
import static com.elvaco.mvp.core.Roles.USER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final OrganisationRepository organisationRepository;
  private final UserDetailsManager userDetailsService;
  private final UserUseCases userUseCases;

  @Autowired
  public UserDatabaseLoader(
    RoleRepository roleRepository,
    OrganisationRepository organisationRepository,
    UserDetailsManager userDetailsService,
    UserUseCases userUseCases
  ) {
    this.roleRepository = roleRepository;
    this.organisationRepository = organisationRepository;
    this.userDetailsService = userDetailsService;
    this.userUseCases = userUseCases;
  }

  @Override
  public void run(String... args) {
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
      new RoleEntity(USER),
      new RoleEntity(ADMIN),
      new RoleEntity(SUPER_ADMIN)
    ));

    Organisation elvaco = new Organisation(1L, "Elvaco", "elvaco");
    Organisation wayneIndustries = new Organisation(2L, "Wayne Industries", "wayne-industries");

    Role user = new Role(USER);
    Role admin = new Role(ADMIN);
    Role superAdmin = new Role(SUPER_ADMIN);

    List<User> users = asList(
      new User(
        "Peter Eriksson",
        "peteri@elvaco.se",
        "peter123",
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
        "user@elvaco.se",
        "user123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Elvis Cohan",
        "elvis.cohan@elvis.com",
        "elvis123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Anna Johansson",
        "annjoh@elvaco.se",
        "anna123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Maria Svensson",
        "marsve@elvaco.se",
        "maria123",
        wayneIndustries,
        singletonList(user)
      ),
      new User(
        "Erik Karlsson",
        "erikar@elvaco.se",
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
      .map(userUseCases::create)
      .map(UserDetailsMapper::toUserDetails)
      .forEach(userDetailsService::createUser);
  }
}
