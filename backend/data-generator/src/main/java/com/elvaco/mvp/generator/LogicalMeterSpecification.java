package com.elvaco.mvp.generator;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;

import static java.util.Arrays.asList;

class LogicalMeterSpecification {

  private static final List<String> GATEWAY_PRODUCT_MODELS = Collections.unmodifiableList(asList(
    "CMe2100",
    "CMe3100"
  ));
  private final Random random;
  private final QuantitySeriesGenerator defaultSeriesGenerator = new
    QuantitySeriesGenerator() {
      @Override
      public double next(@Nullable Double lastValue) {
        double standardDeviation = 5;
        double mean = lastValue == null ? 20 : lastValue;
        return random.nextGaussian() * standardDeviation + mean;
      }
    };
  private MeterDefinition meterDefinition;
  private Duration intervalDuration;
  private MeasurementSpecification measurementSpecification;
  private Organisation organisation;

  LogicalMeterSpecification(Random random) {
    this.measurementSpecification = new MeasurementSpecification(random);
    this.random = random;
  }

  LogicalMeterSpecification withDefinition(MeterDefinition meterDefinition) {
    this.meterDefinition = meterDefinition;
    return this;
  }

  LogicalMeterSpecification withReportInterval(Duration intervalDuration) {
    this.intervalDuration = intervalDuration;
    measurementSpecification = measurementSpecification.withInterval(intervalDuration);
    return this;
  }

  LogicalMeterSpecification withMeasurementsBetween(ZonedDateTime start, ZonedDateTime end) {
    measurementSpecification = measurementSpecification.between(start, end);
    return this;
  }

  LogicalMeterSpecification withMeasurementLossFactor(double lossFactor) {
    measurementSpecification = measurementSpecification.withLossFactor(lossFactor);
    return this;
  }

  LogicalMeterSpecification withOrganisation(Organisation organisation) {
    this.organisation = organisation;
    return this;
  }

  GeneratedData create() {
    UUID logicalMeterUuid = UUID.randomUUID();
    String externalId = meterDefinition.medium + "-" + logicalMeterUuid;
    PhysicalMeter physicalMeter = new PhysicalMeter(
      UUID.randomUUID(),
      organisation,
      generateAddress(),
      externalId,
      meterDefinition.medium,
      "ELV",
      intervalDuration.toMinutes()
    ).withLogicalMeterId(logicalMeterUuid);

    List<Measurement> measurements = new ArrayList<>();
    for (Quantity quantity : meterDefinition.quantities) {
      measurements.addAll(measurementSpecification.createWith(
        quantity,
        defaultSeriesGenerator,
        physicalMeter
      ));
    }

    Gateway gateway = new Gateway(
      UUID.randomUUID(),
      organisation.id,
      generateSerial(),
      pickProductModel()
    );

    return new GeneratedData(new LogicalMeter(
      logicalMeterUuid,
      externalId,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      measurements.get(0).created,
      Collections.singletonList(physicalMeter),
      meterDefinition,
      Collections.singletonList(gateway)
    ), measurements, physicalMeter, gateway);

  }

  private String pickProductModel() {
    return GATEWAY_PRODUCT_MODELS.get(
      random.nextInt(GATEWAY_PRODUCT_MODELS.size() - 1)
    );
  }

  private String generateSerial() {
    return String.format("0016%012d",random.nextInt(Integer.MAX_VALUE));
  }

  private String generateAddress() {
    return String.format("%d", random.nextInt(99_999_999));
  }

}
