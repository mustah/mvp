package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterDefinitionJpaRepository extends JpaRepository<MeterDefinitionEntity, Long> {
  public Optional<MeterDefinitionEntity> findByMedium(String medium);
}
