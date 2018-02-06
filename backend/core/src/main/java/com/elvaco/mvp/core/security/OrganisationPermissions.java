package com.elvaco.mvp.core.security;

import java.util.NoSuchElementException;

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

  public enum Permission {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete");

    private final String name;

    Permission(String name) {
      this.name = name;
    }

    public static Permission fromString(String s) {
      for (Permission p : values()) {
        if (p.name.equalsIgnoreCase(s)) {
          return p;
        }
      }
      throw new NoSuchElementException(s);
    }
  }
}
