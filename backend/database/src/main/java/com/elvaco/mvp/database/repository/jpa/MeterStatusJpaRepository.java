package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.meter.MeterStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterStatusJpaRepository
  extends JpaRepository<MeterStatusEntity, Long> {
}
