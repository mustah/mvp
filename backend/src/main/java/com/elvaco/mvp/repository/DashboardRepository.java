package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.dashboard.DashboardEntity;

public interface DashboardRepository extends JpaRepository<DashboardEntity, Long> {
}
