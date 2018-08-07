package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.measurement.MissingMeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MissingMeasurementPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MissingMeasurementJpaRepository
  extends JpaRepository<MissingMeasurementEntity, MissingMeasurementPk> {

  @Modifying
  @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW CONCURRENTLY missing_measurement")
  void refreshConcurrently();

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW missing_measurement")
  void refreshLocked();

}
