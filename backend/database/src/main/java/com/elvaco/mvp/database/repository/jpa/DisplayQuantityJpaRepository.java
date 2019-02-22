package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.meter.DisplayQuantityEntity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityPk;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DisplayQuantityJpaRepository
  extends JpaRepository<DisplayQuantityEntity, DisplayQuantityPk> {
  void deleteAllByPkMeterDefinitionId(Long id);
}
