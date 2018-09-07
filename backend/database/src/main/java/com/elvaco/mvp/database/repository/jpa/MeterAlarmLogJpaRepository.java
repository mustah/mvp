package com.elvaco.mvp.database.repository.jpa;

import java.util.Collection;

import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;

public interface MeterAlarmLogJpaRepository {

  <S extends MeterAlarmLogEntity> S save(S entity);

  <S extends MeterAlarmLogEntity> Collection<S> save(Iterable<S> alarms);

  Iterable<MeterAlarmLogEntity> findAll();

  void deleteAll();
}
