package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;

import static java.util.stream.Collectors.toList;

public class MockUsers extends MockRepository<User> implements Users {

  public MockUsers(List<User> users) {
    super(users);
  }

  @Override
  protected Optional<Long> getId(User entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  protected User copyWithId(Long id, User entity) {
    return new User(
      id,
      entity.name,
      entity.email,
      entity.password,
      entity.organisation,
      entity.roles
    );
  }

  @Override
  public List<User> findAll() {
    return allMocks();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return filter(u -> u.email.equalsIgnoreCase(email)).findFirst();
  }

  @Override
  public Optional<User> findById(Long id) {
    return filter(u -> Objects.requireNonNull(u.id).equals(id)).findFirst();
  }

  @Override
  public List<User> findByRole(Role role) {
    return filter(u -> u.roles.contains(role)).collect(toList());
  }

  @Override
  public List<User> findByOrganisationId(Long id) {
    return filter(u -> u.organisation.id.equals(id)).collect(toList());
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
