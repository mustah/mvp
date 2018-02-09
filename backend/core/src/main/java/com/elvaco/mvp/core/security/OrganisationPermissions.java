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
    Organisation targetDomainObject,
    Permission permission
  ) {
    return authenticatedUser.isSuperAdmin();
  }

  public boolean isAllowed(
    AuthenticatedUser authenticatedUser,
    User targetDomainObject,
    Permission permission
  ) {
    if (authenticatedUser.isSuperAdmin()) {
      return cannotRemoveLastSuperAdminUser(permission);
    }

    Organisation organisation = targetDomainObject.organisation;
    if (!authenticatedUser.isWithinOrganisation(organisation)) {
      return false;
    }

    if (authenticatedUser.isAdmin()) {
      return true; // admins can do anything on users of the same organisation
    }

    switch (permission) {
      case READ:
        return true;
      case UPDATE:
        return authenticatedUser.getUsername().equalsIgnoreCase(targetDomainObject.email);
      case DELETE:
      case CREATE:
      default:
        return false;
    }
  }

  private boolean cannotRemoveLastSuperAdminUser(Permission permission) {
    return !permission.equals(Permission.DELETE) || users.findByRole(Role.SUPER_ADMIN).size() != 1;
  }
}
