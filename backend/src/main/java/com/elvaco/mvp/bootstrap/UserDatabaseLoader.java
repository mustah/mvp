package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@H2
@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserJpaRepository repository;

  @Autowired
  public UserDatabaseLoader(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    Stream.of(
      new UserEntity("User Fake", "user@elvaco.se", "Bostäder AB"),
      new UserEntity("Elvis Cohan", "elvis.cohan@elvis.com", "Bostäder AB"),
      new UserEntity("Anna Johansson", "annjoh@elvaco.se", "Bostäder AB"),
      new UserEntity("Peter Eriksson", "peteri@elvaco.se", "Elvaco AB"),
      new UserEntity("Maria Svensson", "marsve@elvaco.se", "Bostäder AB"),
      new UserEntity("Erik Karlsson", "erikar@elvaco.se", "Bostäder AB"),
      new UserEntity("Eva Nilsson", "evanil@elvaco.se", "Bostäder AB"),
      new UserEntity("Emil Tirén", "emitir@elvaco.se", "Elvaco AB"),
      new UserEntity("Hanna Sjöstedt", "hansjo@elvaco.se", "Elvaco AB"),
      new UserEntity("Super Admin", "a", "Elvaco AB")
    )
      .forEach(repository::save);
  }
}
