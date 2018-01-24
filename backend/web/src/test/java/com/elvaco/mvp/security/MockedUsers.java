package com.elvaco.mvp.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;

class MockedUsers implements Users {
  private List<User> users;

  public MockedUsers(List<User> users) {
    this.users = new ArrayList<>(users);
  }

  @Override
  public List<User> findAll() {
    return users;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return users.stream().filter(u -> u.email.equalsIgnoreCase(email)).findFirst();
  }

  @Override
  public Optional<User> findById(Long id) {
    return users.stream().filter(u -> u.id.equals(id)).findFirst();
  }

  @Override
  public Optional<Password> findPasswordByUserId(Long userId) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public User save(User user) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void deleteById(Long id) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
