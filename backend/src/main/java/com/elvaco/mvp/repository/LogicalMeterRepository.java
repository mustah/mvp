package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.meter.LogicalMeterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogicalMeterRepository extends JpaRepository<LogicalMeterEntity, Long> {
}
