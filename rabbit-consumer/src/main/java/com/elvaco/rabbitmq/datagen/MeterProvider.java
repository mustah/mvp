package com.elvaco.rabbitmq.datagen;

import org.jfairy.producer.BaseProducer;

import javax.xml.ws.Provider;

public class MeterProvider {
    protected long address = 0;

    protected final BaseProducer baseProducer;
    public MeterProvider(BaseProducer baseProducer) {
        this.baseProducer = baseProducer;
    }

    public Meter get() {
        generateAddress();
        return new Meter(new MetricProvider(baseProducer, baseProducer.trueOrFalse()), address);
    }

    public void generateAddress() {
        if (address != 0) {
            return;
        }
        address = baseProducer.randomBetween(1, 999999);
    }

}
