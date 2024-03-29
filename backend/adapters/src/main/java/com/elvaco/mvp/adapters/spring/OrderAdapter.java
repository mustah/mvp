package com.elvaco.mvp.adapters.spring;

import com.elvaco.mvp.core.spi.data.Order;

class OrderAdapter implements Order {
  private final Direction direction;
  private final String property;
  private final boolean ignoreCase;

  OrderAdapter(String direction, String property) {
    //TODO handle invalid values
    this(Direction.valueOf(direction), property, false);
  }

  private OrderAdapter(Direction direction, String property, boolean ignoreCase) {
    this.direction = direction;
    this.property = property;
    this.ignoreCase = ignoreCase;
  }

  @Override
  public String getProperty() {
    return property;
  }

  @Override
  public Direction getDirection() {
    return direction;
  }

  @Override
  public boolean isIgnoreCase() {
    return ignoreCase;
  }
}
