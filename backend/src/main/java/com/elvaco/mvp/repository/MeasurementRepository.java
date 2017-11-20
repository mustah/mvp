package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MeasurementRepository extends PagingAndSortingRepository<MeasurementEntity, Long> {
  Page<MeasurementEntity> findByQuantity(String quantity, Pageable pageable);

  @Query(value = "SELECT id, physical_meter_id, created, quantity, unit_at(value, ?2) as value FROM measurement WHERE quantity = ?1 ORDER BY ?#{#pageable}",
    countQuery = "SELECT count(id) FROM measurement WHERE quantity = ?1 ORDER BY ?#{#pageable}",
    nativeQuery = true)
  Page<MeasurementEntity> findByQuantityScaled(String quantity, String scaleDimension, Pageable pageable);
}
