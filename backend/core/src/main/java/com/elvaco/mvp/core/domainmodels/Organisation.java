package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Organisation implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = -375927914085016616L;

  public final Long id;
  public final String name;
  public final String code;

  public Organisation(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }

  @Override
  public Long getId() {
    return id;
  }
}
