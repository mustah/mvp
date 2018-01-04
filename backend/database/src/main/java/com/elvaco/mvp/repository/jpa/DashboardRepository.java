package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.dashboard.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<DashboardEntity, Long> {
}
