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

  public static final Role OTC_OTD_ADMIN = new Role("OTC_OTD_ADMIN");
  public static final Role OTC_ADMIN = new Role("OTC_ADMIN");
  public static final Role OTC_USER = new Role("OTC_USER");
  public static final Role OTC_APP_ADMIN = new Role("OTC_APP_ADMIN");
  public static final Role OTC_APP_SERVICE_TECH = new Role("OTC_APP_SERVICE_TECH");
  public static final Role OTC_APP_INSTALLER = new Role("OTC_APP_INSTALLER");

  public static final List<Role> ALL_ROLES = List.of(
    SUPER_ADMIN,
    MVP_USER,
    MVP_ADMIN,
    OTC_ADMIN,
    OTC_USER
  );

  public static final List<Role> MVP_ROLES = List.of(MVP_ADMIN, MVP_USER);
  public static final List<Role> OTC_ROLES = List.of(OTC_OTD_ADMIN, OTC_ADMIN, OTC_USER);

  private static final long serialVersionUID = -7414389623391879883L;

  public final String role;

  @Override
  public String getId() {
    return role;
  }
}
