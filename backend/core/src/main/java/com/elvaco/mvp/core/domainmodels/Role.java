package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Role implements Identifiable<String> {

  public static final Role USER = new Role("USER");
  public static final Role ADMIN = new Role("ADMIN");
  public static final Role SUPER_ADMIN = new Role("SUPER_ADMIN");

  public final String role;

  public Role(String role) {
    this.role = role;
  }

  @Override
  public String getId() {
    return role;
  }
}
