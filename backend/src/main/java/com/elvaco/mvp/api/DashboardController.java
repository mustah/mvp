package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.*;
import com.elvaco.mvp.entity.dashboard.DashboardEntity;
import com.elvaco.mvp.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

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
    List<WidgetDTO> widgets = new ArrayList<>();
    ColoredBoxDTO warning = new ColoredBoxDTO();
    warning.state = "warning";
    warning.value = "95.8";
    warning.unit = "%";
    warning.subtitle = "3567 punkter";
    warning.title = "Insamling";
    warning.url = "/collection";
    widgets.add(warning);

    ColoredBoxDTO critical = new ColoredBoxDTO();
    critical.state = "critical";
    critical.value = "93.5";
    critical.unit = "%";
    critical.subtitle = "3481 punkter";
    critical.title = "Mätvärdeskvalitet";
    critical.url = "/validation";
    widgets.add(critical);

    ColoredBoxDTO ok = new ColoredBoxDTO();
    ok.state = "ok";
    ok.value = "100";
    ok.unit = "%";
    ok.subtitle = "4 st";
    ok.title = "Connectorer";
    ok.url = "/404?connectorsAreTodo";
    widgets.add(ok);

    GraphDTO graph = new GraphDTO();
    graph.title = "Tidsupplösning";
    graph.url = "/404?notSureWhatThisLinkShouldPointAt";
    graph.records.add(new GraphValueDTO("15m", 23.0F));
    graph.records.add(new GraphValueDTO("1h", 10.0F));
    graph.records.add(new GraphValueDTO("24h", 4.0F));
    widgets.add(graph);

    SystemOverviewDTO systemOverviewDTO = new SystemOverviewDTO();
    systemOverviewDTO.title = "Sven's system overview from the DashboardController";
    systemOverviewDTO.widgets = widgets;

    DashboardDTO dashboard = new DashboardDTO();
    dashboard.author = "Sven";
    dashboard.id = 3L;
    dashboard.title = "Sven's dashboard from the DashboardController";
    dashboard.systemOverview = systemOverviewDTO;

    return dashboard;
  }

}
