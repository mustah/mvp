package com.elvaco.mvp.core.util;

import java.io.Serializable;
import java.util.Comparator;

import com.elvaco.mvp.core.domainmodels.Role;

import static com.elvaco.mvp.core.domainmodels.Role.ALL_MVP_ROLES;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

public class RoleComparator implements Comparator<Role>, Serializable {

  @Override
  public int compare(Role role1, Role role2) {
    if (ALL_MVP_ROLES.contains(role1) && ALL_MVP_ROLES.contains(role2)) {
      if (role1.equals(role2)) {
        return 0;
      }

      if (role1.equals(SUPER_ADMIN)) {
        return 1;
      }

      if (role2.equals(SUPER_ADMIN)) {
        return -1;
      }

      if (role1.equals(MVP_ADMIN)) {
        return 1;
      }

      if (role1.equals(MVP_USER)) {
        return -1;
      }
    }

    throw new RuntimeException(
      String.format("Comparing role '%s' to '%s' is not supported", role1, role2)
    );
  }
}
