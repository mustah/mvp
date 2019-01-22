package com.elvaco.mvp.core.security;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.Usernamed;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.util.RoleComparator;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;

public class OrganisationPermissions {

  private static final RoleComparator ROLE_COMPARATOR = new RoleComparator();
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
    @Nullable User beforeUpdate,
    Permission permission
  ) {
    if (!isUserRoleSufficient(authenticatedUser, target, beforeUpdate)) {
      return false;
    }

    if (authenticatedUser.isSuperAdmin()) {
      return permission.isNotDelete()
        || isNotSelf(authenticatedUser, target)
        || isNotLastSuperAdminUser();
    }

    if (!isWithinSameOrganisation(authenticatedUser, target)) {
      return false;
    }

    if (authenticatedUser.isAdmin()) {
      return true; // admins can do anything on users of the same organisation
    }

    switch (permission) {
      case READ:
        return true;
      case UPDATE:
        return isSelf(authenticatedUser, target);
      case DELETE:
      case CREATE:
      default:
        return false;
    }
  }

  private boolean isUserRoleSufficient(
    AuthenticatedUser authenticatedUser, User target, User beforeUpdate
  ) {
    return isTargetRolesGreaterOrEqual(authenticatedUser, target)
      && (beforeUpdate == null || isTargetRolesGreaterOrEqual(authenticatedUser, beforeUpdate));
  }

  private boolean isWithinSameOrganisation(AuthenticatedUser authenticatedUser, User target) {
    return authenticatedUser.isWithinOrganisation(target.organisation.id);
  }

  private boolean isSelf(Usernamed authenticatedUser, Usernamed target) {
    return authenticatedUser.hasSameUsernameAs(target);
  }

  private boolean isNotSelf(Usernamed authenticatedUser, Usernamed target) {
    return !isSelf(authenticatedUser, target);
  }

  private boolean isNotLastSuperAdminUser() {
    return users.findByRole(SUPER_ADMIN).size() != 1;
  }

  private boolean isTargetRolesGreaterOrEqual(
    AuthenticatedUser currentUser, User target
  ) {
    if (currentUser.isSuperAdmin()) {
      return isRoleGreaterOrEqual(SUPER_ADMIN, target.roles);
    } else if (currentUser.isAdmin()) {
      return isRoleGreaterOrEqual(ADMIN, target.roles);
    } else {
      return isRoleGreaterOrEqual(USER, target.roles);
    }
  }

  private boolean isRoleGreaterOrEqual(Role role, List<Role> roleList) {
    return roleList.stream().anyMatch(role2 -> ROLE_COMPARATOR.compare(role, role2) >= 0);
  }
}
