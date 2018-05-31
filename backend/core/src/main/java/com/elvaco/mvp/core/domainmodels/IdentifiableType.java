package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;

public abstract class IdentifiableType<T> implements Identifiable<T>, Serializable {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdentifiableType<?> that = (IdentifiableType<?>) o;
    return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : super.hashCode();
  }
}
