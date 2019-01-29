package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
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
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.VOLUME.name)),
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
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.EXTERNAL_TEMPERATURE.name)),
        RANDOM.nextDouble(15, 40),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.HUMIDITY.name)),
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
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.ENERGY.name)),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 4),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.ENERGY_RETURN.name)),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 1),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.REACTIVE_ENERGY.name)),
        RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.POWER.name)),
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
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.FORWARD_TEMPERATURE.name)),
        tempIn,
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.RETURN_TEMPERATURE.name)),
        tempOut,
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.DIFFERENCE_TEMPERATURE.name)),
        tempIn - tempOut,
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.VOLUME_FLOW.name)),
        RANDOM.nextDouble(0.0, 3.0),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(
          quantityProvider.getByName(Quantity.POWER.name)),
        RANDOM.nextDouble(100.0, 200.0),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.ENERGY.name)),
        RANDOM.nextDouble(1000.0, 3000.0),
        meter
      ),
      new MeasurementEntity(
        created,
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.VOLUME.name)),
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
        quantityEntityMapper.toEntity(quantityProvider.getByName(Quantity.VOLUME.name)),
        value,
        meter
      )
    );
  }
}
