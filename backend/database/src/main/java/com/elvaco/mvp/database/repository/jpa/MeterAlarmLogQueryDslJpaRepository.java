package com.elvaco.mvp.database.repository.jpa;

import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
class MeterAlarmLogQueryDslJpaRepository
  extends BaseQueryDslRepository<MeterAlarmLogEntity, Long>
  implements MeterAlarmLogJpaRepository {

  @Autowired
  MeterAlarmLogQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, MeterAlarmLogEntity.class);
  }
}
