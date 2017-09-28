package com.elvaco.rabbitmq;

import com.elvaco.rabbitmq.datagen.Meter;
import com.elvaco.rabbitmq.datagen.MeterProvider;
import com.elvaco.rabbitmq.datagen.Metric;
import com.google.gson.Gson;
import net.sf.cglib.core.Local;
import org.jfairy.Fairy;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RandomMeterMessageProducer implements Iterator<MeterMessage> {
    Fairy fairy;
    List<Meter> meters;
    RandomMeterMessageProducer() {
        fairy = Fairy.create();
        meters = new ArrayList<>();
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
        meters.add(new MeterProvider(fairy.baseProducer()).get());
    }
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public MeterMessage next() {
        Meter meter = fairy.baseProducer().randomElement(meters);
        Metric metric = meter.metric();
        List<Value> values = new ArrayList<>();
        values.add(new Value()
                .withAccumulated(false)
                .withQuantity(metric.quantity().name())
                .withTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'h:m:ss").format(new Date()).toString())
                .withUnit(metric.unit().name())
                .withValue(metric.value()));
        return new MeterMessage()
                .withMeterId(Long.toString(meter.address()))
                .withOrganisationId("Some organisation")
                .withValues(values);
    }
}
