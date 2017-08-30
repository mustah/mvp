package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.entities.dashboard.DashboardEntity;
import com.elvaco.mvp.repositories.DashboardRepository;

@RestApi
public class DashboardController {

  private final DashboardRepository dashboardRepository;

  @Autowired
  public DashboardController(DashboardRepository dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  @RequestMapping("/dashboards")
  public List<DashboardEntity> dashboards() {
    return dashboardRepository.findAll();
  }
}
