package com.elvaco.mvp.core.domainmodels;

@FunctionalInterface
public interface Usernamed {

  String getUsername();

  default boolean hasSameUsernameAs(Usernamed usernamed) {
    return this.getUsername().equalsIgnoreCase(usernamed.getUsername());
  }
}
