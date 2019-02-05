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

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;

public class UserUseCases {

  private final AuthenticatedUser currentUser;
  private final Users users;
  private final OrganisationPermissions organisationPermissions;
  private final TokenService tokenService;
  private final Organisations organisations;

  public UserUseCases(
    AuthenticatedUser currentUser,
    Users users,
    OrganisationPermissions organisationPermissions,
    TokenService tokenService,
    Organisations organisations
  ) {
    this.currentUser = currentUser;
    this.users = users;
    this.organisationPermissions = organisationPermissions;
    this.tokenService = tokenService;
    this.organisations = organisations;
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
      .filter(user -> organisationPermissions.isAllowed(currentUser, user, user, READ));
  }

  public Optional<User> create(User user) {
    user = user.withOrganisation(organisations.findById(user.organisation.id).orElseThrow());

    if (organisationPermissions.isAllowed(currentUser, user, null, CREATE)) {
      return Optional.of(users.save(user));
    }
    return Optional.empty();
  }

  public Optional<User> update(User user) {
    Optional<User> userBeforeUpdate = users.findById(user.id);
    if (userBeforeUpdate.isPresent()
      && organisationPermissions.isAllowed(currentUser, user, userBeforeUpdate.get(), UPDATE)
    ) {
      return Optional.of(
        removeTokenForUser(users.update(user.withPassword(userBeforeUpdate.get().password)))
      );
    }
    return Optional.empty();
  }

  public Optional<User> delete(UUID userId) {
    return findById(userId)
      .filter(u -> organisationPermissions.isAllowed(currentUser, u, u, DELETE))
      .stream()
      .peek(u -> users.deleteById(u.id))
      .peek(u -> removeTokenForUser(u))
      .findAny();
  }

  public Optional<User> changePassword(UUID userId, String password) {
    Optional<User> originalUser = this.findById(userId);
    if (originalUser.isPresent()) {
      User updatedUser = originalUser.get().withPassword(password);
      if (organisationPermissions.isAllowed(currentUser, updatedUser, originalUser.get(), UPDATE)) {
        return Optional.of(removeTokenForUser(users.save(updatedUser)));
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
