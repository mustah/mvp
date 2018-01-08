package com.elvaco.mvp.bootstrap;

import java.util.List;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.core.Roles;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.RoleRepository;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@H2
@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserJpaRepository repository;
  private final RoleRepository roleRepository;
  private final OrganisationRepository organisationRepository;
  private final UserDetailsManager userDetailsService;

  @Autowired
  public UserDatabaseLoader(
    UserJpaRepository repository,
    RoleRepository roleRepository,
    OrganisationRepository organisationRepository,
    UserDetailsManager userDetailsService
  ) {
    this.repository = repository;
    this.roleRepository = roleRepository;
    this.organisationRepository = organisationRepository;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void run(String... args) {
    OrganisationEntity elvaco = new OrganisationEntity(
      1L,
      "Elvaco",
      "elvaco"
    );
    OrganisationEntity wayneIndustries = new OrganisationEntity(
      2L,
      "Wayne Industries",
      "wayne-industries"
    );

    organisationRepository.save(asList(elvaco, wayneIndustries));

    RoleEntity user = new RoleEntity(Roles.USER);
    RoleEntity admin = new RoleEntity(Roles.ADMIN);
    RoleEntity superAdmin = new RoleEntity(Roles.SUPER_ADMIN);

    roleRepository.save(asList(
      user,
      admin,
      superAdmin
    ));

    List<UserEntity> users = asList(
      new UserEntity(
        "Peter Eriksson",
        "peteri@elvaco.se",
        "peter123",
        elvaco,
        singletonList(user)
      ),
      new UserEntity(
        "Emil Tirén",
        "emitir@elvaco.se",
        "emil123",
        elvaco,
        asList(user, admin, superAdmin)
      ),
      new UserEntity(
        "Hanna Sjöstedt",
        "hansjo@elvaco.se",
        "hanna123",
        elvaco,
        asList(user, admin)
      ),
      new UserEntity(
        "User Fake",
        "user@elvaco.se",
        "user123",
        wayneIndustries,
        singletonList(user)
      ),
      new UserEntity(
        "Elvis Cohan",
        "elvis.cohan@elvis.com",
        "elvis123",
        wayneIndustries,
        singletonList(user)
      ),
      new UserEntity(
        "Anna Johansson",
        "annjoh@elvaco.se",
        "anna123",
        wayneIndustries,
        singletonList(user)
      ),
      new UserEntity(
        "Maria Svensson",
        "marsve@elvaco.se",
        "maria123",
        wayneIndustries,
        singletonList(user)
      ),
      new UserEntity(
        "Erik Karlsson",
        "erikar@elvaco.se",
        "erik123",
        wayneIndustries,
        singletonList(user)
      ),
      new UserEntity(
        "Eva Nilsson",
        "evanil@elvaco.se",
        "eva123",
        wayneIndustries,
        asList(user, superAdmin)
      ),
      new UserEntity(
        "Super Admin",
        "superadmin@elvaco.se",
        "admin123",
        elvaco,
        singletonList(superAdmin)
      ),
      new UserEntity("Developer", "user", "password", elvaco, singletonList(superAdmin))
    );

    repository.save(users);

    users.stream()
      .map(this::toUserDetails)
      .forEach(userDetailsService::createUser);
  }

  private UserDetails toUserDetails(UserEntity user) {
    return User.withUsername(user.email)
      .password(user.password)
      .roles(user.roles.stream()
               .map(r -> r.role)
               .collect(toList())
               .toArray(new String[user.roles.size()]))
      .build();
  }
}
