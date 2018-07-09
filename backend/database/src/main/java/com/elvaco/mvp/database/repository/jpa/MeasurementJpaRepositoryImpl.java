package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.measurement.QMeasurementEntity.measurementEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseQueryDslRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  private static final QMeasurementEntity MEASUREMENT = measurementEntity;
  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  @Autowired
  MeasurementJpaRepositoryImpl(EntityManager entityManager) {
    super(entityManager, MeasurementEntity.class);
  }

  @Override
  public Optional<MeasurementEntity> findBy(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  ) {
    Predicate predicate = MEASUREMENT.physicalMeter.id.eq(physicalMeterId)
      .and(MEASUREMENT.quantity.eq(quantity))
      .and(MEASUREMENT.created.eq(created));

    return Optional.ofNullable(
      createQuery(predicate)
        .select(path)
        .join(MEASUREMENT.physicalMeter, PHYSICAL_METER)
        .fetchJoin()
        .fetchOne()
    );
  }
}

