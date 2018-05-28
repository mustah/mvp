package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/map-markers")
public class MapMarkerController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping("/meters")
  public List<MapMarkerDto> meterMapMarkers(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return logicalMeterUseCases.findAll(requestParametersOf(requestParams))
      .stream()
      .map(LogicalMeterDtoMapper::toMapMarkerDto)
      .collect(toList());
  }

  @GetMapping("/meters/{id}")
  public MapMarkerDto findMapMarker(@PathVariable UUID id) {
    return logicalMeterUseCases.findById(id)
      .map(LogicalMeterDtoMapper::toMapMarkerDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }
}
