package com.elvaco.rabbitmq.datagen;

public class Metric {

  private final Quantity quantity;
  private final Unit unit;
  private final double value;

  Metric(Quantity quantity, Unit unit, double value) {
    this.quantity = quantity;
    this.unit = unit;
    this.value = value;
  }

  public double value() {
    return value;
  }

  public Unit unit() {
    return unit;
  }

  public Quantity quantity() {
    return quantity;
  }
}
