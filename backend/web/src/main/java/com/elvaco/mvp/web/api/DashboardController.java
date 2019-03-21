package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.exception.DashboardNotFound;
import com.elvaco.mvp.web.exception.WidgetNotFound;
import com.elvaco.mvp.web.mapper.DashboardDtoMapper;
import com.elvaco.mvp.web.mapper.WidgetDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.web.mapper.DashboardDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.DashboardDtoMapper.toDto;
import static com.elvaco.mvp.web.mapper.WidgetDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.WidgetDtoMapper.toDto;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/dashboards")
public class DashboardController {

  private final DashboardUseCases dashboardUseCases;
  private final AuthenticatedUser authenticatedUser;

  @GetMapping
  public List<DashboardDto> getAllDashboards() {
    return dashboardUseCases.findDashboardsForCurrentUser().stream()
      .map(DashboardDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("widgets")
  public List<WidgetDto> getWidgetsForDashboard(
    @RequestParam(name = "dashboardId") UUID dashboardId
  ) {
    return dashboardUseCases.findWidgetsForCurrentUserAndDashboard(dashboardId).stream()
      .map(WidgetDtoMapper::toDto)
      .collect(toList());
  }

  @PostMapping
  public ResponseEntity<DashboardDto> addDashboard(@RequestBody DashboardDto dashboardDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(toDto(dashboardUseCases.save(
        toDomainModel(
          dashboardDto,
          authenticatedUser.getUserId(),
          authenticatedUser.getOrganisationId()
        ))));
  }

  @DeleteMapping("{dashboardId}")
  public DashboardDto deleteDashboard(@PathVariable UUID dashboardId) {
    return toDto(dashboardUseCases.deleteDashboard(dashboardId)
      .orElseThrow(() -> new DashboardNotFound(dashboardId)));
  }

  @PostMapping("widgets")
  public ResponseEntity<WidgetDto> addWidget(
    @RequestBody WidgetDto widgetDto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(toDto(dashboardUseCases.save(toDomainModel(
        widgetDto,
        authenticatedUser.getUserId(),
        authenticatedUser.getOrganisationId()
      ))));
  }

  @DeleteMapping("widgets/{widgetId}")
  public WidgetDto deleteWidget(@PathVariable UUID widgetId) {
    return toDto(dashboardUseCases.deleteWidget(widgetId)
      .orElseThrow(() -> new WidgetNotFound(widgetId)));
  }

  @PutMapping
  public DashboardDto updateDashboard(@RequestBody DashboardDto dashboardDto) {
    return toDto(dashboardUseCases.update(
      toDomainModel(
        dashboardDto,
        authenticatedUser.getUserId(),
        authenticatedUser.getOrganisationId()
      )));
  }

  @PutMapping("widgets")
  public WidgetDto updateWidget(@RequestBody WidgetDto widgetDto) {
    return toDto(dashboardUseCases.save(
      toDomainModel(
        widgetDto,
        authenticatedUser.getUserId(),
        authenticatedUser.getOrganisationId()
      )));
  }
}

