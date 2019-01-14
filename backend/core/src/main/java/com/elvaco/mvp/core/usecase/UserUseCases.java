package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;

public class UserUseCases {

  private final AuthenticatedUser currentUser;
  private final Users users;
  private final OrganisationPermissions organisationPermissions;
  private final TokenService tokenService;

  public UserUseCases(
    AuthenticatedUser currentUser,
    Users users,
    OrganisationPermissions organisationPermissions,
    TokenService tokenService
  ) {
    this.currentUser = currentUser;
    this.users = users;
    this.organisationPermissions = organisationPermissions;
    this.tokenService = tokenService;
  }

  public List<User> findAll() {
    if (currentUser.isSuperAdmin()) {
      return users.findAll();
    } else {
      return users.findByOrganisationId(currentUser.getOrganisationId());
    }
  }

  public Optional<User> findById(UUID id) {
    return users.findById(id)
      .filter(user -> organisationPermissions.isAllowed(currentUser, user, READ));
  }

  public Optional<User> create(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, CREATE)) {
      return Optional.of(users.save(user));
    }
    return Optional.empty();
  }

  public Optional<User> update(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, UPDATE)) {
      return users.findPasswordByUserId(user.id)
        .map(user::withPassword)
        .map(users::update)
        .map(this::removeTokenForUser);
    }
    return Optional.empty();
  }

  public void delete(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, DELETE)) {
      users.deleteById(user.id);
      removeTokenForUser(user);
    }
  }

  public Optional<User> changePassword(User user) {
    if (organisationPermissions.isAllowed(currentUser, user, UPDATE)) {
      if (users.findPasswordByUserId(user.id).isPresent()) {
        return Optional.of(removeTokenForUser(users.save(user)));
      }
    }
    return Optional.empty();
  }

  private User removeTokenForUser(User user) {
    if (!currentUser.hasSameUsernameAs(user)) {
      tokenService.removeTokenByUsername(user.getUsername());
    }
    return user;
  }
}
