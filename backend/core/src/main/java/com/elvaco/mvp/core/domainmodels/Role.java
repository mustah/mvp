package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Role {

  public static final String USER = "USER";
  public static final String ADMIN = "ADMIN";
  public static final String SUPER_ADMIN = "SUPER_ADMIN";

  public final String role;

  public Role(String role) {
    this.role = role;
  }

  public static Role admin() {
    return new Role(ADMIN);
  }

  public static Role user() {
    return new Role(USER);
  }

  public static Role superAdmin() {
    return new Role(SUPER_ADMIN);
  }
}
