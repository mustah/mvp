package com.elvaco.mvp.core.security;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.Usernamed;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.util.RoleComparator;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.OTC_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

@RequiredArgsConstructor
public class UserPermissions {

  private static final RoleComparator ROLE_COMPARATOR = new RoleComparator();

  private final Users users;

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

    if (!authenticatedUser.isWithinOrganisation(target.organisation.id)
      && !isWithinSubOrganisation(authenticatedUser, target)
    ) {
      return false;
    }

    // admins can do anything on users of the same organisation
    if (authenticatedUser.isMvpAdmin() || authenticatedUser.isOtcAdmin()) {
      return true;
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

  public boolean isAllowedToRead(
    AuthenticatedUser authenticatedUser,
    User target,
    @Nullable User beforeUpdate
  ) {
    return isAllowed(authenticatedUser, target, beforeUpdate, Permission.READ);
  }

  public boolean isAllowedToDelete(
    AuthenticatedUser authenticatedUser,
    User target,
    @Nullable User beforeUpdate
  ) {
    return isAllowed(authenticatedUser, target, beforeUpdate, Permission.DELETE);
  }

  public boolean isAllowedToCreate(
    AuthenticatedUser authenticatedUser,
    User target,
    @Nullable User beforeUpdate
  ) {
    return isAllowed(authenticatedUser, target, beforeUpdate, Permission.CREATE);
  }

  public boolean isAllowedToUpdate(
    AuthenticatedUser authenticatedUser,
    User target,
    @Nullable User beforeUpdate
  ) {
    return isAllowed(authenticatedUser, target, beforeUpdate, Permission.UPDATE);
  }

  private boolean isUserRoleSufficient(
    AuthenticatedUser authenticatedUser,
    User target,
    @Nullable User beforeUpdate
  ) {
    return isTargetRolesGreaterOrEqual(authenticatedUser, target)
      && (beforeUpdate == null || isTargetRolesGreaterOrEqual(authenticatedUser, beforeUpdate));
  }

  private boolean isWithinSubOrganisation(AuthenticatedUser authenticatedUser, User target) {
    return target.organisation.parent != null
      && authenticatedUser.isWithinOrganisation(target.organisation.parent.id);
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

  private boolean isTargetRolesGreaterOrEqual(AuthenticatedUser currentUser, User target) {
    if (currentUser.isSuperAdmin()) {
      return isRoleGreaterOrEqual(SUPER_ADMIN, target.roles);
    } else if (currentUser.isMvpAdmin()) {
      return isRoleGreaterOrEqual(MVP_ADMIN, target.roles);
    } else if (currentUser.isOtcAdmin()) {
      return isRoleGreaterOrEqual(OTC_ADMIN, target.roles);
    } else {
      return isRoleGreaterOrEqual(MVP_USER, target.roles);
    }
  }

  private boolean isRoleGreaterOrEqual(Role role, List<Role> roleList) {
    return roleList.stream().anyMatch(role2 -> ROLE_COMPARATOR.compare(role, role2) >= 0);
  }
}
