package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;

import static java.util.stream.Collectors.toList;

public class MockUsers extends MockRepository<Long, User> implements Users {

  public MockUsers(List<User> users) {
    users.forEach(this::saveMock);
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
  protected Long generateId() {
    return nextId();
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
  public List<User> findByOrganisationId(UUID organisationId) {
    return filter(u -> u.organisation.id.equals(organisationId)).collect(toList());
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
