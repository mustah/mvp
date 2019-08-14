package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.UserPermissions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserUseCases {

  private final AuthenticatedUser currentUser;
  private final Users users;
  private final UserPermissions userPermissions;
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
      .filter(user -> userPermissions.isAllowedToRead(currentUser, user, user));
  }

  public Optional<User> create(User user) {
    var userWithOrganisation = organisations.findById(user.organisation.id)
      .map(user::withOrganisation)
      .orElseThrow();

    return Optional.of(userWithOrganisation)
      .filter(u -> userPermissions.isAllowedToCreate(currentUser, u, null))
      .map(users::save);
  }

  public Optional<User> update(User user) {
    return users.findById(user.id)
      .filter(u -> userPermissions.isAllowedToUpdate(currentUser, user, u))
      .map(u -> user.withPassword(u.password))
      .map(users::update)
      .map(this::removeTokenForUser);
  }

  public Optional<User> delete(UUID userId) {
    return findById(userId)
      .filter(u -> userPermissions.isAllowedToDelete(currentUser, u, u)).stream()
      .peek(u -> users.deleteById(u.id))
      .peek(this::removeTokenForUser)
      .findAny();
  }

  public Optional<User> changePassword(UUID userId, String password) {
    return findById(userId)
      .filter(u -> userPermissions.isAllowedToUpdate(currentUser, u, u))
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
