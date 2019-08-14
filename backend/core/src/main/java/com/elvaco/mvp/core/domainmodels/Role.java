package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Role implements Identifiable<String>, Serializable {

  public static final Role SUPER_ADMIN = new Role("SUPER_ADMIN");
  public static final Role MVP_USER = new Role("MVP_USER");
  public static final Role MVP_ADMIN = new Role("MVP_ADMIN");
  public static final Role OTC_ADMIN = new Role("OTC_ADMIN");

  public static final List<Role> ALL_ROLES = List.of(
    SUPER_ADMIN,
    MVP_USER,
    MVP_ADMIN,
    OTC_ADMIN
  );

  private static final long serialVersionUID = -7414389623391879883L;

  public final String role;

  @Override
  public String getId() {
    return role;
  }
}
