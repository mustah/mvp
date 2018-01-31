package com.elvaco.mvp.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;

import static java.util.stream.Collectors.toList;

class MockUsers implements Users {

  private final List<User> users;

  MockUsers(List<User> users) {
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
    return users.stream().filter(u -> Objects.requireNonNull(u.id).equals(id)).findFirst();
  }

  @Override
  public List<User> findByRole(Role role) {
    return users.stream().filter(u -> u.roles.contains(role)).collect(toList());
  }

  @Override
  public List<User> findByOrganisation(Organisation organisation) {
    return users.stream().filter(u -> u.organisation.id.equals(organisation.id)).collect(toList());
  }

  @Override
  public Optional<Password> findPasswordByUserId(Long userId) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public User create(User user) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public User update(User user) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void deleteById(Long id) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
