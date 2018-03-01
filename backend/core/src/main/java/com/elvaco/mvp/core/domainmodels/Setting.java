package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.ToString;

@ToString
public class Setting implements Identifiable<UUID> {

  private final UUID id;
  public final String name;
  public final String value;

  public Setting(UUID id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
