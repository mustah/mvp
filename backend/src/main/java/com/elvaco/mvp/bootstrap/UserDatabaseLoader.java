package com.elvaco.mvp.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entities.user.UserEntity;
import com.elvaco.mvp.repositories.UserRepository;

@Component

@Profile("default")
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserRepository repository;

  @Autowired
  public UserDatabaseLoader(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<UserEntity> users = new ArrayList<>();
    users.add(new UserEntity("Elvis", "Cohan"));
    users.add(new UserEntity("Anna", "Johansson"));
    users.add(new UserEntity("Peter", "Eriksson"));
    users.add(new UserEntity("Maria", "Svensson"));
    users.add(new UserEntity("Erik", "Karlsson"));
    users.add(new UserEntity("Eva", "Nilsson"));

    users.forEach(user -> {
      user.company = "Bostäder AB";
      repository.save(user);
    });
  }
}
