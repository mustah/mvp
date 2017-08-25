package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.dashboard.DashboardEntity;

public interface DashboardRepository extends JpaRepository<DashboardEntity, Long> {
}
