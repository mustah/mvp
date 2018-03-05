package com.elvaco.mvp.core.security;

public enum Permission {

  CREATE,
  READ,
  UPDATE,
  DELETE;

  public boolean isNotDelete() {
    return this != DELETE;
  }
}
