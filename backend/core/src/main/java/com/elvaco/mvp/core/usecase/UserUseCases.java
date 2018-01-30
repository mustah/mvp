package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;

import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.CREATE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.DELETE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.READ;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.UPDATE;

public class UserUseCases {

  private final AuthenticatedUser currentUser;
  private final Users users;
  private final OrganisationPermissions organisationPermissions;

  public UserUseCases(
    AuthenticatedUser currentUser,
    Users users,
    OrganisationPermissions organisationPermissions
  ) {
    this.currentUser = currentUser;
    this.users = users;
    this.organisationPermissions = organisationPermissions;
  }

  public List<User> findAll() {
    if (currentUser.isSuperAdmin()) {
      return users.findAll();
    }
    return users.findByOrganisation(currentUser.getOrganisation());
  }

  public Optional<User> findByEmail(String email) {
    return users.findByEmail(email)
      .filter(
        user -> organisationPermissions.isAllowed(currentUser, user, READ)
      );
  }

  public Optional<User> findById(Long id) {
    return users.findById(id)
      .filter(
        user -> organisationPermissions.isAllowed(currentUser, user, READ)
      );
  }

  public Optional<User> create(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, CREATE)) {
      return Optional.of(users.create(user));
    }
    return Optional.empty();
  }

  public Optional<User> update(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, UPDATE)) {
      return users.findPasswordByUserId(user.id)
        .map(user::withPassword)
        .map(users::update);
    }
    return Optional.empty();
  }

  public void delete(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, DELETE)) {
      users.deleteById(user.id);
    }
  }
}
