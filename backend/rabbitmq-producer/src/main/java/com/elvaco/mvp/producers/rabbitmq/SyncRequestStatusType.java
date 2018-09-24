package com.elvaco.mvp.producers.rabbitmq;

public enum SyncRequestStatusType {
  UNKNOWN, PENDING, COMPLETED;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
