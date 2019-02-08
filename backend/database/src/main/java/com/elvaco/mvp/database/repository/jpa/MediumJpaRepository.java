package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.MediumEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediumJpaRepository extends JpaRepository<MediumEntity, Long> {
  Optional<MediumEntity> findByName(String name);
}
