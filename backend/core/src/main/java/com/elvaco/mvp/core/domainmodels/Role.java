package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Role {

  public final String role;

  public Role(String role) {
    this.role = role;
  }
}
