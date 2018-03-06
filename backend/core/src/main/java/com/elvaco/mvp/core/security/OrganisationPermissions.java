package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;

public class OrganisationPermissions {

  private final Users users;

  public OrganisationPermissions(Users users) {
    this.users = users;
  }

  public boolean isAllowed(
    AuthenticatedUser authenticatedUser,
    Organisation target,
    Permission permission
  ) {
    return authenticatedUser.isSuperAdmin();
  }

  public boolean isAllowed(
    AuthenticatedUser authenticatedUser,
    User target,
    Permission permission
  ) {
    if (authenticatedUser.isSuperAdmin()) {
      return permission.isNotDelete()
             || isNotSelf(authenticatedUser, target)
             || isNotLastSuperAdminUser();
    }

    Organisation organisation = target.organisation;
    if (!authenticatedUser.isWithinOrganisation(organisation.id)) {
      return false;
    }

    if (authenticatedUser.isAdmin()) {
      return true; // admins can do anything on users of the same organisation
    }

    switch (permission) {
      case READ:
        return true;
      case UPDATE:
        return authenticatedUser.getUsername().equalsIgnoreCase(target.email);
      case DELETE:
      case CREATE:
      default:
        return false;
    }
  }

  private boolean isNotSelf(AuthenticatedUser authenticatedUser, User target) {
    return !authenticatedUser.getUsername().equals(target.email);
  }

  private boolean isNotLastSuperAdminUser() {
    return users.findByRole(Role.SUPER_ADMIN).size() != 1;
  }
}
