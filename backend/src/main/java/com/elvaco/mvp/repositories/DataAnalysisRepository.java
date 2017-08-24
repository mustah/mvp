package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.dataanalysis.DataAnalysisEntity;

public interface DataAnalysisRepository extends JpaRepository<DataAnalysisEntity, Long> {
}
