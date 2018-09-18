package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseQueryDslRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

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
    Predicate predicate = MEASUREMENT.id.physicalMeter.id.eq(physicalMeterId)
      .and(MEASUREMENT.id.quantity.name.eq(quantity))
      .and(MEASUREMENT.id.created.eq(created));

    return Optional.ofNullable(
      createQuery(predicate)
        .select(path)
        .join(MEASUREMENT.id.physicalMeter, PHYSICAL_METER)
        .fetchJoin()
        .fetchOne()
    );
  }
}

