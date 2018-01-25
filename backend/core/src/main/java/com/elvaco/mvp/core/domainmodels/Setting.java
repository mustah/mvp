package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.ToString;

@ToString
public class Setting {
  @Nullable
  public final Long id;
  public final String name;
  public final String value;

  public Setting(String name, String value) {
    this(null, name, value);
  }

  public Setting(@Nullable Long id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }
}
