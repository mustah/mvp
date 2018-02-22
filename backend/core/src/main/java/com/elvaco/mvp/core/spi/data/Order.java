package com.elvaco.mvp.core.spi.data;

public interface Order {
  String getProperty();

  Direction getDirection();

  boolean isIgnoreCase();

  enum Direction {
    ASC, DESC
  }
}
