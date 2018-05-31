package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MapMarkersDto;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.groupingBy;

@UtilityClass
public class MapMarkersDtoMapper {

  public static MapMarkersDto fromGateways(List<Gateway> gateways) {
    Map<String, List<MapMarkerDto>> markers = gateways
      .stream()
      .flatMap(gateway -> gateway.meters
        .stream()
        .filter(hasHighConfidence())
        .map(logicalMeter -> logicalMeter.location.getCoordinate())
        .map(coordinate -> new MapMarkerDto(
            gateway.id,
            coordinate.getLatitude(),
            coordinate.getLongitude(),
            gateway.currentStatus().status
          )
        ))
      .collect(groupingBy(status()));

    return new MapMarkersDto(markers);
  }

  public static MapMarkersDto fromLogicalMeters(List<LogicalMeter> logicalMeters) {
    Map<String, List<MapMarkerDto>> markers = logicalMeters.stream()
      .filter(hasHighConfidence())
      .map(logicalMeter -> {
        GeoCoordinate coordinate = logicalMeter.location.getCoordinate();
        return new MapMarkerDto(
          logicalMeter.id,
          coordinate.getLatitude(),
          coordinate.getLongitude(),
          logicalMeter.currentStatus()
        );
      })
      .collect(groupingBy(status()));

    return new MapMarkersDto(markers);
  }

  private static Predicate<LogicalMeter> hasHighConfidence() {
    return logicalMeter -> logicalMeter.location.hasHighConfidence();
  }

  private static Function<MapMarkerDto, String> status() {
    return marker -> marker.status.name;
  }
}
