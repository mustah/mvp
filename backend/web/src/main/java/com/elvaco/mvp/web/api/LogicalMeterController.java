package com.elvaco.mvp.web.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeteringRequestPublisher meteringRequestPublisher;
  private final PropertiesUseCases propertiesUseCases;

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeter(
    @PathVariable UUID id,
    @RequestParam(
      value = "before",
      required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(
      value = "after",
      defaultValue = "1970-01-01T00:00:00Z"
    ) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after
  ) {
    RequestParameters parameters = new RequestParametersAdapter()
      .replace(ID, id.toString())
      .replace(BEFORE, before != null ? before.toString() : ZonedDateTime.now().toString())
      .replace(AFTER, after.toString());

    return logicalMeterUseCases.findBy(parameters)
      .map(LogicalMeterDtoMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @PostMapping("{id}/synchronize")
  public ResponseEntity<Void> synchronizeMeter(@PathVariable UUID id) {
    logicalMeterUseCases.findById(id)
      .map(this::sync)
      .orElseThrow(() -> new MeterNotFound(id));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PostMapping("/synchronize")
  public ResponseEntity<Void> synchronizeMetersByIds(@RequestBody List<UUID> logicalMetersIds) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAllIds(ID, logicalMetersIds);

    logicalMeterUseCases.findAllBy(parameters).forEach(this::sync);

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping
  public org.springframework.data.domain.Page<PagedLogicalMeterDto> logicalMeters(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams);
    PageableAdapter adapter = new PageableAdapter(pageable);

    Page<LogicalMeter> page = logicalMeterUseCases.findAll(parameters, adapter);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(LogicalMeterDtoMapper::toPagedDto);
  }

  @GetMapping("/details")
  public org.springframework.data.domain.Page<PagedLogicalMeterDto> logicalMetersWithDetails(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams);
    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAllWithStatuses(parameters);

    return new PageImpl<>(logicalMeters, pageable, logicalMeters.size())
      .map(LogicalMeterDtoMapper::toPagedDetailsDto);
  }

  @DeleteMapping("{id}")
  public LogicalMeterDto deleteMeter(@PathVariable UUID id) {
    return LogicalMeterDtoMapper.toDto(
      logicalMeterUseCases.deleteById(id)
        .orElseThrow(() -> new MeterNotFound(id))
    );
  }

  private LogicalMeter sync(LogicalMeter logicalMeter) {
    meteringRequestPublisher.request(logicalMeter);
    propertiesUseCases.forceUpdateGeolocation(logicalMeter.id, logicalMeter.organisationId);
    return logicalMeter;
  }
}
