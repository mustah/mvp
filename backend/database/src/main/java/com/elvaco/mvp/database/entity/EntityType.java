package com.elvaco.mvp.database.entity;

import java.io.Serializable;

public abstract class EntityType<T> implements Serializable {

  public abstract T getId();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntityType<?> that = (EntityType<?>) o;
    return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : super.hashCode();
  }
}
