package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

public interface Users {

  List<User> findAll();

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);

  Optional<String> findPasswordByUserId(UUID userId);

  User create(User user);

  User update(User user);

  User updateWithNewPassword(User user);

  void deleteById(UUID id);

  List<User> findByRole(Role role);

  List<User> findByOrganisationId(UUID organisationId);
}
