package com.elvaco.mvp.core.domainmodels;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Role implements Identifiable<String> {

  public static final Role MVP_USER = new Role("MVP_USER");
  public static final Role MVP_ADMIN = new Role("MVP_ADMIN");
  public static final Role SUPER_ADMIN = new Role("SUPER_ADMIN");

  public static final List<Role> ALL_ROLES = List.of(
    MVP_USER,
    MVP_ADMIN,
    SUPER_ADMIN
  );

  public static final List<String> ALL_ROLE_IDS = ALL_ROLES.stream()
    .map(r -> r.role)
    .collect(toList());

  public final String role;

  @Override
  public String getId() {
    return role;
  }
}
