package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.dto.ColoredBoxDTO;
import com.elvaco.mvp.dto.DashboardDTO;
import com.elvaco.mvp.dto.GraphDTO;
import com.elvaco.mvp.dto.GraphValueDTO;
import com.elvaco.mvp.dto.SystemOverviewDTO;
import com.elvaco.mvp.entity.dashboard.DashboardEntity;
import com.elvaco.mvp.repository.DashboardRepository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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

  @RequestMapping("/dashboards/current")
  public DashboardDTO myDashboard() {
    ColoredBoxDTO warning = new ColoredBoxDTO();
    warning.type = "collection";
    warning.state = "warning";
    warning.value = "95.8";
    warning.unit = "%";
    warning.subtitle = "3567 punkter";
    warning.title = "Insamling";

    ColoredBoxDTO critical = new ColoredBoxDTO();
    critical.type = "measurementQuality";
    critical.state = "critical";
    critical.value = "93.5";
    critical.unit = "%";
    critical.subtitle = "3481 punkter";
    critical.title = "Mätvärdeskvalitet";

    GraphDTO graph = new GraphDTO();
    graph.title = "Tidsupplösning";
    graph.records.add(new GraphValueDTO("15m", 23.0F));
    graph.records.add(new GraphValueDTO("1h", 10.0F));
    graph.records.add(new GraphValueDTO("24h", 4.0F));

    SystemOverviewDTO systemOverviewDTO = new SystemOverviewDTO();
    systemOverviewDTO.title = "Sven's system overview from the DashboardController";
    systemOverviewDTO.indicators = asList(warning, critical);
    systemOverviewDTO.donutGraphs = singletonList(graph);

    DashboardDTO dashboard = new DashboardDTO();
    dashboard.author = "Sven";
    dashboard.id = 3L;
    dashboard.title = "Sven's dashboard from the DashboardController";
    dashboard.systemOverview = systemOverviewDTO;

    return dashboard;
  }
}
