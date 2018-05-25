package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeteringRequestPublisher meteringRequestPublisher;
  private final LogicalMeterDtoMapper logicalMeterDtoMapper;

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeter(@PathVariable UUID id) {
    return logicalMeterUseCases.findByIdWithMeasurements(id)
      .map(logicalMeterDtoMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @PostMapping("{id}/synchronize")
  public ResponseEntity<Void> synchronizeMeter(@PathVariable UUID id) {
    LogicalMeter logicalMeter = logicalMeterUseCases.findById(id)
      .orElseThrow(() -> new MeterNotFound(id));

    meteringRequestPublisher.request(logicalMeter);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping("/map-markers")
  public List<MapMarkerDto> mapMarkers(@RequestParam MultiValueMap<String, String> requestParams) {
    return logicalMeterUseCases.findAll(requestParametersOf(requestParams))
      .stream()
      .map(LogicalMeterDtoMapper::toMapMarkerDto)
      .collect(toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<LogicalMeterDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAllWithMeasurements(parameters, adapter);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(logicalMeterDtoMapper::toDto);
  }

  @DeleteMapping("{id}")
  public LogicalMeterDto deleteMeter(@PathVariable UUID id) {
    return logicalMeterDtoMapper.toDto(
      logicalMeterUseCases.deleteById(id)
        .orElseThrow(() -> new MeterNotFound(id))
    );
  }
}
