package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.dataanalysis.DataAnalysisEntity;

public interface DataAnalysisRepository extends JpaRepository<DataAnalysisEntity, Long> {
}
