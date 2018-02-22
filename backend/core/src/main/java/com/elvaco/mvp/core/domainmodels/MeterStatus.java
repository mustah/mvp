package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

public class MeterStatus {
  @Nullable
  public final Long id;
  public final String name;

  public MeterStatus(String name) {
    this(null, name);
  }

  public MeterStatus(@Nullable Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
