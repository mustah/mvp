package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface MeasurementRepository extends PagingAndSortingRepository<MeasurementEntity, Long> {
  Page<MeasurementEntity> findByQuantity(String quantity, Pageable pageable);

  Page<MeasurementEntity> findByQuantityScaled(String quantity, String scaleDimension, Pageable pageable);
}
