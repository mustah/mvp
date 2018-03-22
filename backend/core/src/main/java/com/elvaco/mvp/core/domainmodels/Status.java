package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

public class Status {
  @Nullable
  public final Long id;
  public final String name;

  public Status(String name) {
    this(null, name);
  }

  public Status(@Nullable Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
