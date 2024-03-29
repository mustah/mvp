package com.elvaco.mvp.web.api;

import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MapUseCases;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.MapMarkersDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import com.elvaco.mvp.web.mapper.MapMarkerDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor
@RestApi("/api/v1/map-markers")
public class MapMarkerController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MapUseCases mapUseCases;

  @GetMapping("/meters")
  public MapMarkersDto meterMapMarkers(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams);
    return toMapMarkersDto(mapUseCases.findAllMeterMapMarkers(parameters));
  }

  @GetMapping("/gateways")
  public MapMarkersDto gatewayMapMarkers(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams);
    return toMapMarkersDto(mapUseCases.findAllGatewayMapMarkers(parameters));
  }

  @GetMapping("/meters/{logicalMeterId}")
  public ResponseEntity<MapMarkerWithStatusDto> findMeterMapMarker(
    @PathVariable UUID logicalMeterId
  ) {
    LogicalMeter logicalMeter = logicalMeterUseCases.findById(logicalMeterId)
      .orElseThrow(() -> new MeterNotFound(logicalMeterId));

    MapMarkerWithStatusDto mapMarker = LogicalMeterDtoMapper.toMapMarkerDto(logicalMeter);
    return mapMarker == null
      ? ResponseEntity.noContent().build()
      : ResponseEntity.ok(mapMarker);
  }

  private static MapMarkersDto toMapMarkersDto(Set<MapMarker> mapMarkers) {
    return new MapMarkersDto(
      mapMarkers.stream()
        .map(MapMarkerDtoMapper::toDto)
        .collect(groupingBy(m -> m.status))
    );
  }
}
