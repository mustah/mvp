package com.elvaco.rabbitmq.datagen;

import org.jfairy.producer.BaseProducer;

public class MeterProvider {

  private long address = 0;

  private final BaseProducer baseProducer;

  public MeterProvider(BaseProducer baseProducer) {
    this.baseProducer = baseProducer;
  }

  public Meter get() {
    generateAddress();
    return new Meter(new MetricProvider(baseProducer, baseProducer.trueOrFalse()), address);
  }

  private void generateAddress() {
    if (address != 0) {
      return;
    }
    address = baseProducer.randomBetween(1, 999999);
  }
}
