package com.elvaco.rabbitmq.datagen;

public class Metric {
    private Quantity quantity;
    private Unit unit;
    private double value;
    public Metric(Quantity quantity, Unit unit, double value) {
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
