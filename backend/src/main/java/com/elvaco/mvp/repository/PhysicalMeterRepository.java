package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PhysicalMeterRepository extends PagingAndSortingRepository<PhysicalMeterEntity, Long> {
}
