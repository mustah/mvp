package com.elvaco.mvp.core.util;

import java.io.Serializable;
import java.util.Comparator;

import com.elvaco.mvp.core.domainmodels.Role;

import static com.elvaco.mvp.core.domainmodels.Role.ALL_ROLES;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.OTC_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

public class RoleComparator implements Comparator<Role>, Serializable {

  private static final int HAS_ACCESS = 1;
  private static final int HAS_NO_ACCESS = -HAS_ACCESS;

  @Override
  public int compare(Role role1, Role role2) {
    if (ALL_ROLES.contains(role1) && ALL_ROLES.contains(role2)) {
      if (role1.equals(role2)) {
        return 0;
      }

      if (role1.equals(SUPER_ADMIN)) {
        return HAS_ACCESS;
      }

      if (role2.equals(SUPER_ADMIN)) {
        return HAS_NO_ACCESS;
      }

      if (role1.equals(MVP_ADMIN) && role2.equals(OTC_ADMIN)) {
        return HAS_NO_ACCESS;
      }

      if (role1.equals(OTC_ADMIN) && role2.equals(MVP_ADMIN)) {
        return HAS_NO_ACCESS;
      }

      if (role1.equals(MVP_ADMIN)) {
        return HAS_ACCESS;
      }

      if (role1.equals(MVP_USER)) {
        return HAS_NO_ACCESS;
      }
    }

    throw new RuntimeException(
      String.format("Comparing role '%s' to '%s' is not supported", role1, role2)
    );
  }
}
