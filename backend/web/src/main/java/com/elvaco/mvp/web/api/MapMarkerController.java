package com.elvaco.mvp.web.api;

import java.util.UUID;

import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@RestApi("/api/v1/map-markers")
public class MapMarkerController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping("/meters/{id}")
  public MapMarkerDto findMapMarker(@PathVariable UUID id) {
    return logicalMeterUseCases.findById(id)
      .map(LogicalMeterDtoMapper::toMapMarkerDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }
}
