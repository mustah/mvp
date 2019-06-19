package com.elvaco.mvp.core.util;

import java.io.Serializable;
import java.util.Comparator;

import com.elvaco.mvp.core.domainmodels.Role;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

public class RoleComparator implements Comparator<Role>, Serializable {

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

      if (role1.role.equals(MVP_ADMIN.role)) {
        return 1;
      }

      if (role1.role.equals(MVP_USER.role)) {
        return -1;
      }
    }

    throw new RuntimeException(
      String.format("Comparing role '%s' to '%s' is not supported", role1.role, role2)
    );
  }

  private boolean isRoleImplemented(String role) {
    return Role.ALL_ROLE_IDS.contains(role);
  }
}
