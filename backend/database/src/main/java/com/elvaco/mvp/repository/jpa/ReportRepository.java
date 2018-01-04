package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.report.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
}
