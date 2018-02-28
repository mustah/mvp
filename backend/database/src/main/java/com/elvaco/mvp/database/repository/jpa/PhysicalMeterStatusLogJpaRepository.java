package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalMeterStatusLogJpaRepository
  extends JpaRepository<PhysicalMeterStatusLogEntity, Long> {

  List<PhysicalMeterStatusLogEntity> findAllByPhysicalMeterIdIn(List<Long> physicalMeterIds);
}
