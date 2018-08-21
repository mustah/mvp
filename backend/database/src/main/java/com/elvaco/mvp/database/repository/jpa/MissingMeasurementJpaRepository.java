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

  @Transactional
  @Modifying
  @Query(
    nativeQuery = true,
    value =
      "DO \n"
      + "$do$ \n"
      + "BEGIN \n"
      + "  SET LOCAL lock_timeout = '500ms';"
      + "  IF (SELECT relispopulated FROM pg_class WHERE relname = 'missing_measurement') THEN \n"
      + "    REFRESH MATERIALIZED VIEW CONCURRENTLY missing_measurement; \n"
      + "  ELSE \n"
      + "    REFRESH MATERIALIZED VIEW missing_measurement; \n"
      + "  END IF; \n"
      + "END \n"
      + "$do$"
  )
  void refresh();
}
