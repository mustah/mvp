package com.elvaco.rabbitmq.datagen;

import org.jfairy.producer.BaseProducer;

import java.util.ArrayList;
import java.util.List;

public class MetricProvider {
    private BaseProducer baseProducer;
    private QuantityAndUnit quantityAndUnit;
    private boolean accumulated;
    private double lastValue;

    private static class QuantityAndUnit {
        private Quantity quantity;
        private Unit unit;
        public QuantityAndUnit(Quantity quantity, Unit unit) {
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private static List<QuantityAndUnit> quantityAndUnitList = new ArrayList<QuantityAndUnit>();
    static {
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.Energy, Unit.Wh));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.Volume, Unit.m3));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.Power, Unit.W));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.Flow, Unit.m3ph));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.ForwardTemperature, Unit.DegreesCelsius));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.ReturnTemperature, Unit.DegreesCelsius));
        quantityAndUnitList.add(new QuantityAndUnit(Quantity.DifferenceTemperature, Unit.DegreesKelvin));
    }
    public MetricProvider(BaseProducer baseProducer, boolean accumulated) {
        this.baseProducer = baseProducer;
        this.accumulated = accumulated;
    }

    public Metric get() {
        generateQuantityAndUnit();

        double val;
        if (accumulated) {
            val = baseProducer.randomBetween(lastValue, 10000);
        } else {
            val = baseProducer.randomBetween(0, 10000);
        }
        return new Metric(quantityAndUnit.quantity, quantityAndUnit.unit, val);

    }

    private void generateQuantityAndUnit()  {
        if (quantityAndUnit != null) {
            return;
        }
        quantityAndUnit = baseProducer.randomElement(quantityAndUnitList);
    }
}
