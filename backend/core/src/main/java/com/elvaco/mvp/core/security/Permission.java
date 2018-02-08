package com.elvaco.mvp.core.security;

import java.util.NoSuchElementException;

public enum Permission {

  CREATE("create"),
  READ("read"),
  UPDATE("update"),
  DELETE("delete");

  private final String name;

  Permission(String name) {
    this.name = name;
  }

  public static Permission fromString(String s) {
    for (Permission p : values()) {
      if (p.name.equalsIgnoreCase(s)) {
        return p;
      }
    }
    throw new NoSuchElementException(s);
  }
}
