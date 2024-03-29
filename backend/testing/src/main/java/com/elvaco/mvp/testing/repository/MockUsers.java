package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class MockUsers extends MockRepository<UUID, User> implements Users {

  public MockUsers(List<User> users) {
    users.forEach(this::saveMock);
  }

  @Override
  protected User copyWithId(UUID id, User user) {
    return UserBuilder.from(user).id(id).build();
  }

  @Override
  protected UUID generateId(User entity) {
    return randomUUID();
  }

  @Override
  public List<User> findAll() {
    return allMocks();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return filter(u -> u.hasSameUsernameAs(() -> email)).findFirst();
  }

  @Override
  public Optional<User> findById(UUID id) {
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
  public Optional<String> findPasswordByUserId(UUID userId) {
    return filter(user -> Objects.equals(user.id, user.id))
      .map(user -> user.password)
      .findFirst();
  }

  @Override
  public User save(User user) {
    return saveMock(user);
  }

  @Override
  public User update(User user) {
    return saveMock(user);
  }

  @Override
  public void deleteById(UUID id) {
    deleteMockById(id);
  }
}
