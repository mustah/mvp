package com.elvaco.mvp.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entities.user.User;
import com.elvaco.mvp.repositories.UserRepository;

@Component
public class UserDatabaseLoader implements CommandLineRunner {

  private final UserRepository repository;

  @Autowired
  public UserDatabaseLoader(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<User> users = new ArrayList<>();
    users.add(new User("Elvis", "Cohan"));
    users.add(new User("Anna", "Johansson"));
    users.add(new User("Peter", "Eriksson"));
    users.add(new User("Maria", "Svensson"));
    users.add(new User("Erik", "Karlsson"));
    users.add(new User("Eva", "Nilsson"));

    users.forEach(user -> {
      user.company = "Bostäder AB";
      repository.save(user);
    });
  }
}
