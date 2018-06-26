package com.elvaco.mvp.database.repository.jpa;

import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseQueryDslRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  MeasurementJpaRepositoryImpl(EntityManager entityManager) {
    super(entityManager, MeasurementEntity.class);
  }
}

