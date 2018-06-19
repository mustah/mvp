package com.elvaco.mvp.testdata;

import java.util.Objects;
import java.util.UUID;

public class IdStatus {

  private final UUID id;
  private final String status;

  public IdStatus(UUID id, String status) {
    this.id = id;
    this.status = status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IdStatus)) {
      return false;
    }
    IdStatus idStatus = (IdStatus) o;
    return Objects.equals(id, idStatus.id)
      && Objects.equals(status, idStatus.status);
  }

  @Override
  public String toString() {
    return "IdStatus{"
      + "id=" + id
      + ", status=" + status + '}';
  }
}
