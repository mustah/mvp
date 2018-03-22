package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.meter.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusJpaRepository
  extends JpaRepository<StatusEntity, Long> {
}
