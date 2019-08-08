package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;

@RequiredArgsConstructor
public class UserUseCases {

  private final AuthenticatedUser currentUser;
  private final Users users;
  private final OrganisationPermissions organisationPermissions;
  private final TokenService tokenService;
  private final Organisations organisations;

  public List<User> findAll() {
    if (currentUser.isSuperAdmin()) {
      return users.findAll();
    } else {
      return users.findByOrganisationId(currentUser.getOrganisationId());
    }
  }

  public Optional<User> findById(UUID id) {
    return users.findById(id)
      .filter(user -> organisationPermissions.isAllowed(currentUser, user, user, READ));
  }

  public Optional<User> create(User user) {
    var userWithOrganisation = organisations.findById(user.organisation.id)
      .map(user::withOrganisation)
      .orElseThrow();

    return Optional.of(userWithOrganisation)
      .filter(u -> organisationPermissions.isAllowed(currentUser, u, null, CREATE))
      .map(users::save);
  }

  public Optional<User> update(User user) {
    return users.findById(user.id)
      .filter(u -> organisationPermissions.isAllowed(currentUser, user, u, UPDATE))
      .map(u -> user.withPassword(u.password))
      .map(users::update)
      .map(this::removeTokenForUser);
  }

  public Optional<User> delete(UUID userId) {
    return findById(userId)
      .filter(u -> organisationPermissions.isAllowed(currentUser, u, u, DELETE)).stream()
      .peek(u -> users.deleteById(u.id))
      .peek(this::removeTokenForUser)
      .findAny();
  }

  public Optional<User> changePassword(UUID userId, String password) {
    return findById(userId)
      .filter(u -> organisationPermissions.isAllowed(currentUser, u, u, UPDATE))
      .map(u -> u.withPassword(password))
      .map(users::save)
      .map(this::removeTokenForUser);
  }

  private User removeTokenForUser(User user) {
    if (!currentUser.hasSameUsernameAs(user)) {
      tokenService.removeTokenByUsername(user.getUsername());
    }
    return user;
  }
}
