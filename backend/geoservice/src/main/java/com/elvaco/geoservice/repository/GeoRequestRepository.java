package com.elvaco.geoservice.repository;

import com.elvaco.geoservice.repository.entity.GeoRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GeoRequestRepository extends PagingAndSortingRepository<GeoRequestEntity, Long> {

  public Page<GeoRequestEntity> findByOrderByCreatedAsc(Pageable pageable);

}
