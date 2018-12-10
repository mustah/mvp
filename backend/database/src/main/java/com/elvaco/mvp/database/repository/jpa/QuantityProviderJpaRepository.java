package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantityProviderJpaRepository
  extends JpaRepository<QuantityEntity, Long> {

  Optional<QuantityEntity> findByName(String name);
}
