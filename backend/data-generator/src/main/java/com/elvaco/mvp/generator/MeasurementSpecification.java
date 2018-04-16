package com.elvaco.mvp.generator;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;

class MeasurementSpecification {

  private final Random random;
  private double lossFactor;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private Duration intervalDuration;

  MeasurementSpecification(Random random) {
    this.random = random;
    lossFactor = 0.0;
  }

  Collection<? extends Measurement> createWith(
    Quantity quantity,
    QuantitySeriesGenerator seriesGenerator,
    PhysicalMeter physicalMeter
  ) {
    List<Measurement> generatedMeasurements = new ArrayList<>();
    ZonedDateTime t = start.truncatedTo(ChronoUnit.HOURS);
    MeasurementValue value = new MeasurementValue(null, null);
    while (t.isBefore(end)) {
      value = new MeasurementValue(seriesGenerator.next(value.value), t.toInstant());
      if (random.nextDouble() > lossFactor) {
        generatedMeasurements.add(new Measurement(
          null,
          value.when.atZone(start.getZone()),
          quantity.name,
          value.value,
          quantity.defaultPresentationUnit(),
          physicalMeter
        ));
      }
      t = t.plusMinutes(intervalDuration.toMinutes());
    }

    return generatedMeasurements;
  }

  MeasurementSpecification withLossFactor(double lossFactor) {
    this.lossFactor = lossFactor;
    return this;
  }

  MeasurementSpecification between(ZonedDateTime start, ZonedDateTime end) {
    this.start = start;
    this.end = end;
    return this;
  }

  MeasurementSpecification withInterval(Duration intervalDuration) {
    this.intervalDuration = intervalDuration;
    return this;
  }
}
