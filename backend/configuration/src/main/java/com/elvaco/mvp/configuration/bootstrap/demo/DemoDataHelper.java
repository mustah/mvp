package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import lombok.RequiredArgsConstructor;

import static java.util.Arrays.asList;

@RequiredArgsConstructor
public class DemoDataHelper {

  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
  private final QuantityProvider quantityProvider;
  private final QuantityEntityMapper quantityEntityMapper;

  public List<MeasurementEntity> waterMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    return List.of(
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.VOLUME),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2),
        meter
      )
    );
  }

  public List<MeasurementEntity> roomSensorMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter
  ) {
    return asList(
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.EXTERNAL_TEMPERATURE),
        RANDOM.nextDouble(15, 40),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.HUMIDITY),
        RANDOM.nextDouble(40, 90),
        meter
      )
    );
  }

  public List<MeasurementEntity> electricityMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    return asList(
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.ENERGY),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 4),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.ENERGY_RETURN),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 1),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.REACTIVE_ENERGY),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.POWER),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 3),
        meter
      )
    );
  }

  public List<MeasurementEntity> heatMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter
  ) {
    double tempIn = RANDOM.nextDouble(1.2, 37.5);
    double tempOut = tempIn - RANDOM.nextDouble(0.5, 10.0);

    return asList(
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.FORWARD_TEMPERATURE),
        tempIn,
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.RETURN_TEMPERATURE),
        tempOut,
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.DIFFERENCE_TEMPERATURE),
        tempIn - tempOut,
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.VOLUME_FLOW),
        RANDOM.nextDouble(0.0, 3.0),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.POWER),
        RANDOM.nextDouble(100.0, 200.0),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.ENERGY),
        RANDOM.nextDouble(1000.0, 3000.0),
        meter
      ),
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.VOLUME),
        RANDOM.nextDouble(1.0, 3.0),
        meter
      )
    );
  }

  public List<MeasurementEntity> gasMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    double value = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2);

    return List.of(
      new MeasurementEntity(
        created,
        getQuantityEntity(Quantity.VOLUME),
        value,
        meter
      )
    );
  }

  private QuantityEntity getQuantityEntity(Quantity quantity) {
    return quantityEntityMapper.toEntity(quantityProvider.getByNameOrThrow(quantity.name));
  }
}
