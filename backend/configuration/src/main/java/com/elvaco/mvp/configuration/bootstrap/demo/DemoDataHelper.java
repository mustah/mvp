package com.elvaco.mvp.configuration.bootstrap.demo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@UtilityClass
public class DemoDataHelper {

  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

  public static List<MeasurementEntity> gasMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    double value = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2);

    return singletonList(
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.VOLUME.name)),
        new MeasurementUnit(Quantity.VOLUME.presentationUnit(), value),
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
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.FORWARD_TEMPERATURE.name)),
        new MeasurementUnit(Quantity.FORWARD_TEMPERATURE.presentationUnit(), tempIn),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.RETURN_TEMPERATURE.name)),
        new MeasurementUnit(Quantity.RETURN_TEMPERATURE.presentationUnit(), tempOut),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.DIFFERENCE_TEMPERATURE.name)),
        new MeasurementUnit(Quantity.DIFFERENCE_TEMPERATURE.presentationUnit(), tempIn - tempOut),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.VOLUME_FLOW.name)),
        new MeasurementUnit(Quantity.VOLUME_FLOW.presentationUnit(), RANDOM.nextDouble(0.0, 3.0)),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.POWER.name)),
        new MeasurementUnit(Quantity.POWER.presentationUnit(), RANDOM.nextDouble(100.0, 200.0)),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.ENERGY.name)),
        new MeasurementUnit(Quantity.ENERGY.presentationUnit(), RANDOM.nextDouble(1000.0, 3000.0)),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.VOLUME.name)),
        new MeasurementUnit(Quantity.VOLUME.presentationUnit(), RANDOM.nextDouble(1.0, 3.0)),
        meter
      )
    );
  }

  public static List<MeasurementEntity> waterMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    double value = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2);

    return singletonList(
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.VOLUME.name)),
        new MeasurementUnit(Quantity.VOLUME.presentationUnit(), value),
        meter
      )
    );
  }

  public static List<MeasurementEntity> electricityMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter,
    double previousMeterReading
  ) {
    double energy = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 4);
    double energyReturn = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 1);
    double energyReactive = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 2);
    double power = RANDOM.nextDouble(previousMeterReading, previousMeterReading + 3);

    return asList(
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.ENERGY.name)),
        new MeasurementUnit(Quantity.ENERGY.presentationUnit(), energy),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.ENERGY_RETURN.name)),
        new MeasurementUnit(Quantity.ENERGY_RETURN.presentationUnit(), energyReturn),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.REACTIVE_ENERGY.name)),
        new MeasurementUnit(Quantity.REACTIVE_ENERGY.presentationUnit(), energyReactive),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(Quantity.POWER.name)),
        new MeasurementUnit(Quantity.POWER.presentationUnit(), power),
        meter
      )
    );
  }

  public static List<MeasurementEntity> roomSensorMeasurement(
    ZonedDateTime created,
    PhysicalMeterEntity meter
  ) {
    return asList(
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.EXTERNAL_TEMPERATURE.name)),
        new MeasurementUnit(
          Quantity.EXTERNAL_TEMPERATURE.presentationUnit(),
          RANDOM.nextDouble(15, 40)
        ),
        meter
      ),
      new MeasurementEntity(
        created,
        QuantityEntityMapper.toEntity(
          QuantityAccess.singleton().getByName(Quantity.HUMIDITY.name)),
        new MeasurementUnit(Quantity.HUMIDITY.presentationUnit(), RANDOM.nextDouble(40, 90)),
        meter
      )
    );
  }
}
