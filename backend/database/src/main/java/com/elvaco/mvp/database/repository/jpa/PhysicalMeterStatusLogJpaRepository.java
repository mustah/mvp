package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalMeterStatusLogJpaRepository
  extends
  QuerydslPredicateExecutor<PhysicalMeterStatusLogEntity>,
  JpaRepository<PhysicalMeterStatusLogEntity, Long> {
}
