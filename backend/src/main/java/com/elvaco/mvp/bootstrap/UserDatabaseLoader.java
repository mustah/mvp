package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.UserRepository;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@InMemory
@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserRepository repository;

  @Autowired
  public UserDatabaseLoader(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
        new UserEntity("User", "Fake", "user@elvaco.se", "Bostäder AB"),
        new UserEntity("Elvis", "Cohan", "elvis.cohan@elvis.com", "Bostäder AB"),
        new UserEntity("Anna", "Johansson", "annjoh@elvaco.se", "Bostäder AB"),
        new UserEntity("Peter", "Eriksson", "peteri@elvaco.se", "Elvaco AB"),
        new UserEntity("Maria", "Svensson", "marsve@elvaco.se", "Bostäder AB"),
        new UserEntity("Erik", "Karlsson", "erikar@elvaco.se", "Bostäder AB"),
        new UserEntity("Eva", "Nilsson", "evanil@elvaco.se", "Bostäder AB"),
        new UserEntity("Emil", "Tirén", "emitir@elvaco.se", "Elvaco AB"),
        new UserEntity("Hanna", "Sjöstedt", "hansjo@elvaco.se", "Elvaco AB"),
        new UserEntity("Super", "Admin", "a", "Elvaco AB")
    )
        .forEach(repository::save);
  }
}
