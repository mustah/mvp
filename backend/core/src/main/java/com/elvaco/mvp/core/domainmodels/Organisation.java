package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Organisation implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -375927914085016616L;

  public final UUID id;
  public final String name;
  public final String code;

  public Organisation(UUID id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
