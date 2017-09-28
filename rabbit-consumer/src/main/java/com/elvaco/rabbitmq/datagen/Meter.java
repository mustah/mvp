package com.elvaco.rabbitmq.datagen;

public class Meter {
    private final long address;
    private final MetricProvider metricProvider;
    public Meter(MetricProvider metricProvider, long address) {
        this.address = address;
        this.metricProvider = metricProvider;
    }

    public long address() {
        return address;
    }

    public Metric metric() {
        return metricProvider.get();
    }
}
