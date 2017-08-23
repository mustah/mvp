package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.dashboard.Dashboard;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
}
