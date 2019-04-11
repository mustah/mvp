package com.elvaco.mvp.testdata;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;

import static java.util.stream.Collectors.toList;

public class MeasurementSeriesBuilder {

  private ZonedDateTime startTime;
  private Quantity quantity;
  private TemporalAmount interval;
  public LogicalMeter logicalMeter;
  private PhysicalMeter physicalMeter;
  private DoubleStream valueStream;

  Collection<Measurement> build() {
    validateMembers();

    ZonedDateTime[] t = {startTime};
    return valueStream.mapToObj(v -> {
        Measurement m = Measurement.builder()
          .readoutTime(t[0])
          .value(v)
          .physicalMeter(getPhysicalMeter(t[0]))
          .quantity(quantity.name)
          .unit(quantity.storageUnit)
          .build();

        t[0] = t[0].plus(Optional.ofNullable(interval)
          .orElseGet(() -> Duration.ofMinutes(getPhysicalMeter(t[0]).readIntervalMinutes)));
        return m;
      }
    ).collect(toList());
  }

  public MeasurementSeriesBuilder startingAt(ZonedDateTime time) {
    this.startTime = time;
    return this;
  }

  public MeasurementSeriesBuilder withQuantity(Quantity quantity) {
    this.quantity = quantity;
    return this;
  }

  public MeasurementSeriesBuilder withInterval(TemporalAmount interval) {
    this.interval = interval;
    return this;
  }

  public MeasurementSeriesBuilder forMeter(LogicalMeter logicalMeter) {
    this.logicalMeter = logicalMeter;
    return this;
  }

  public MeasurementSeriesBuilder forPhysicalMeter(PhysicalMeter physicalMeter) {
    this.physicalMeter = physicalMeter;
    return this;
  }

  MeasurementSeriesBuilder withValuesFrom(DoubleSupplier valueSupplier) {
    this.valueStream = DoubleStream.generate(valueSupplier);
    return this;
  }

  public MeasurementSeriesBuilder withValues(double... values) {
    this.valueStream = Arrays.stream(values);
    return this;
  }

  private PhysicalMeter getPhysicalMeter(ZonedDateTime t) {
    if (physicalMeter != null) {
      return physicalMeter;
    } else {
      return logicalMeter.activePhysicalMeter(t)
        .orElseThrow(() ->
          new IllegalStateException("No active physical meter present at '" + t.toString() + "'")
        );
    }
  }

  private void validateMembers() {
    if (startTime == null) {
      throw new IllegalStateException("startTime should not be null");
    }

    if (logicalMeter == null) {
      throw new IllegalStateException("meter should not be null");
    }

    if (valueStream == null) {
      throw new IllegalStateException("values should not be null");
    }

    if (quantity == null) {
      throw new IllegalStateException("quantity should not be null");
    }
  }
}
