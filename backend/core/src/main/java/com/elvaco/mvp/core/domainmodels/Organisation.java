package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Organisation {

  public final Long id;
  public final String name;
  public final String code;

  public Organisation(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }
}
