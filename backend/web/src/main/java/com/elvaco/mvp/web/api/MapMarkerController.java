package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.MapMarkersDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import com.elvaco.mvp.web.mapper.MapMarkersDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RequiredArgsConstructor
@RestApi("/api/v1/map-markers")
public class MapMarkerController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final GatewayUseCases gatewayUseCases;

  @GetMapping("/meters")
  public MapMarkersDto meterMapMarkers(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    List<LogicalMeter> meters = logicalMeterUseCases.findAll(requestParametersOf(requestParams));
    return MapMarkersDtoMapper.fromLogicalMeters(meters);
  }

  @GetMapping("/gateways")
  public MapMarkersDto gatewayMapMarkers(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    List<Gateway> gateways = gatewayUseCases.findAll(requestParametersOf(requestParams));
    return MapMarkersDtoMapper.fromGateways(gateways);
  }

  @GetMapping("/meters/{logicalMeterId}")
  public MapMarkerWithStatusDto findMeterMapMarker(@PathVariable UUID logicalMeterId) {
    LogicalMeter logicalMeter = logicalMeterUseCases.findById(logicalMeterId)
      .orElseThrow(() -> new MeterNotFound(logicalMeterId));

    return LogicalMeterDtoMapper.toMapMarkerDto(logicalMeter);
  }
}
