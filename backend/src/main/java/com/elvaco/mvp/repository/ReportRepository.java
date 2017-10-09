package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.report.ReportEntity;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
}
