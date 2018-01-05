package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.core.Roles;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@H2
@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserJpaRepository repository;
  private final OrganisationRepository organisationRepository;
  private final UserDetailsManager userDetailsService;

  @Autowired
  public UserDatabaseLoader(
    UserJpaRepository repository,
    OrganisationRepository organisationRepository,
    UserDetailsManager userDetailsService
  ) {
    this.repository = repository;
    this.organisationRepository = organisationRepository;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void run(String... args) {
    OrganisationEntity elvaco = new OrganisationEntity(1L, "Elvaco", "elvaco");
    OrganisationEntity wayneIndustries = new OrganisationEntity(
      2L,
      "Wayne Industries",
      "wayne-industries"
    );

    Stream.of(elvaco, wayneIndustries)
      .forEach(organisationRepository::save);

    Stream.of(
      new UserEntity("Peter Eriksson", "peteri@elvaco.se", elvaco),
      new UserEntity("Emil Tirén", "emitir@elvaco.se", elvaco),
      new UserEntity("Hanna Sjöstedt", "hansjo@elvaco.se", elvaco),
      new UserEntity("User Fake", "user@elvaco.se", wayneIndustries),
      new UserEntity("Elvis Cohan", "elvis.cohan@elvis.com", wayneIndustries),
      new UserEntity("Anna Johansson", "annjoh@elvaco.se", wayneIndustries),
      new UserEntity("Maria Svensson", "marsve@elvaco.se", wayneIndustries),
      new UserEntity("Erik Karlsson", "erikar@elvaco.se", wayneIndustries),
      new UserEntity("Eva Nilsson", "evanil@elvaco.se", wayneIndustries),
      new UserEntity("Super Admin", "superadmin@elvaco.se", elvaco)
    )
      .forEach(repository::save);

    Stream.of(
      User.withUsername("user")
        .password("password")
        .roles(Roles.USER)
        .build(),
      User.withUsername("evanil@elvaco.se")
        .password("eva123")
        .roles(Roles.USER)
        .build(),
      User.withUsername("hansjo@elvaco.se")
        .password("hanna123")
        .roles(Roles.USER, Roles.ADMIN)
        .build(),
      User.withUsername("emitir@elvaco.se")
        .password("emil123")
        .roles(Roles.USER, Roles.ADMIN)
        .build(),
      User.withUsername("a")
        .password("a")
        .roles(Roles.USER, Roles.ADMIN)
        .build()
    )
      .forEach(userDetailsService::createUser);
  }
}
