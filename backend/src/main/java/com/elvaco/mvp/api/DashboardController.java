package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.dto.ColoredBoxDto;
import com.elvaco.mvp.dto.DashboardDto;
import com.elvaco.mvp.dto.SystemOverviewDto;
import com.elvaco.mvp.entity.dashboard.DashboardEntity;
import com.elvaco.mvp.repository.jpa.DashboardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Arrays.asList;

@RestApi("/api/dashboards")
public class DashboardController {

  private final DashboardRepository dashboardRepository;

  @Autowired
  public DashboardController(DashboardRepository dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  @RequestMapping
  public List<DashboardEntity> dashboards() {
    return dashboardRepository.findAll();
  }

  @RequestMapping("/current")
  public DashboardDto myDashboard() {
    ColoredBoxDto warning = new ColoredBoxDto();
    warning.type = "collection";
    warning.state = "warning";
    warning.value = "95.8";
    warning.unit = "%";
    warning.subtitle = "3567 punkter";
    warning.title = "Insamling";

    ColoredBoxDto critical = new ColoredBoxDto();
    critical.type = "measurementQuality";
    critical.state = "critical";
    critical.value = "93.5";
    critical.unit = "%";
    critical.subtitle = "3481 punkter";
    critical.title = "Validering";

    SystemOverviewDto systemOverviewDto = new SystemOverviewDto();
    systemOverviewDto.title = "Sven's system overview from the DashboardController";
    systemOverviewDto.indicators = asList(warning, critical);

    DashboardDto dashboard = new DashboardDto();
    dashboard.author = "Sven";
    dashboard.id = 3L;
    dashboard.title = "Sven's dashboard from the DashboardController";
    dashboard.systemOverview = systemOverviewDto;

    return dashboard;
  }
}
