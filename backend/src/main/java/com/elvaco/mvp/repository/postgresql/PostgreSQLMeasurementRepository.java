package com.elvaco.mvp.repository.postgresql;

import com.elvaco.mvp.config.Compose;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Compose
public interface PostgreSQLMeasurementRepository extends MeasurementRepository {

  @Query(value = "SELECT id, physical_meter_id, created, quantity, unit_at(value, ?2) as value FROM measurement WHERE quantity = ?1 ORDER BY ?#{#pageable}",
    countQuery = "SELECT count(id) FROM measurement WHERE quantity = ?1 ORDER BY ?#{#pageable}",
    nativeQuery = true)
  @Override
  Page<MeasurementEntity> findByQuantityScaled(String quantity, String scaleDimension, Pageable pageable);
}
