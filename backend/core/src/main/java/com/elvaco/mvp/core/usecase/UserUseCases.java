package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.User;

public class UserUseCases {

  private final Users users;

  public UserUseCases(Users users) {
    this.users = users;
  }

  public List<User> findAll() {
    return users.findAll();
  }

  public Optional<User> findByEmail(String email) {
    return users.findByEmail(email);
  }

  public Optional<User> findById(Long id) {
    return users.findById(id);
  }

  public User create(User user) {
    return users.save(user);
  }

  public Optional<User> update(User user) {
    return users.findPasswordByUserId(user.id)
      .map(user::withPassword)
      .map(users::save);
  }

  public void deleteById(Long id) {
    users.deleteById(id);
  }
}
