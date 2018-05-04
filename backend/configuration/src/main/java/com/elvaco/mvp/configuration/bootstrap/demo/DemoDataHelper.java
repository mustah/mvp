package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;

@UtilityClass
public class DemoDataHelper {
  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

  public static List<MeasurementEntity> gasMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    double value = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2);

    return asList(
      new MeasurementEntity(
        created,
        Quantity.VOLUME.name,
        value,
        Quantity.VOLUME.presentationUnit(),
        meter
      )
    );
  }

  public static List<MeasurementEntity> heatMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter
  ) {
    double tempIn = RANDOM.nextDouble(1.2, 37.5);
    double tempOut = tempIn - RANDOM.nextDouble(0.5, 10.0);

    return asList(
      new MeasurementEntity(
        created,
        Quantity.FORWARD_TEMPERATURE.name,
        tempIn,
        Quantity.FORWARD_TEMPERATURE.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.RETURN_TEMPERATURE.name,
        tempOut,
        Quantity.RETURN_TEMPERATURE.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.DIFFERENCE_TEMPERATURE.name,
        tempIn - tempOut,
        Quantity.DIFFERENCE_TEMPERATURE.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.VOLUME_FLOW.name,
        RANDOM.nextDouble(0.0, 3.0),
        Quantity.VOLUME_FLOW.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.POWER.name,
        RANDOM.nextDouble(100.0, 200.0),
        Quantity.POWER.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.ENERGY.name,
        RANDOM.nextDouble(1000.0, 3000.0),
        Quantity.ENERGY.presentationUnit(),
        meter
      ),
      new MeasurementEntity(
        created,
        Quantity.VOLUME.name,
        RANDOM.nextDouble(1.0, 3.0),
        Quantity.VOLUME.presentationUnit(),
        meter
      )
    );
  }
}
