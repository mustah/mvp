package com.elvaco.mvp.core.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;

public class RoleComparator implements Comparator<Role>, Serializable {

  private static final List<String> ALL_ROLES = List.of(USER.role, ADMIN.role, SUPER_ADMIN.role);

  @Override
  public int compare(Role role1, Role role2) {
    if (isRoleImplemented(role1.role) && isRoleImplemented(role2.role)) {
      if (role1.role.equals(role2.role)) {
        return 0;
      }

      if (role1.role.equals(SUPER_ADMIN.role)) {
        return 1;
      }

      if (role2.role.equals(SUPER_ADMIN.role)) {
        return -1;
      }

      if (role1.role.equals(ADMIN.role)) {
        return 1;
      }

      if (role1.role.equals(USER.role)) {
        return -1;
      }
    }

    throw new RuntimeException(
      String.format("Comparing role '%s' to '%s' is not supported", role1.role, role2)
    );
  }

  private boolean isRoleImplemented(String role) {
    return ALL_ROLES.contains(role);
  }
}
