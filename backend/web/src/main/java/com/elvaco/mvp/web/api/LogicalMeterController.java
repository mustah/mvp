package com.elvaco.mvp.web.api;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
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
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeteringRequestPublisher meteringRequestPublisher;

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
    if (before == null) {
      before = ZonedDateTime.now();
    }
    RequestParameters parameters = new RequestParametersAdapter();
    parameters.replace("id", id.toString());
    parameters.replace("before", before.toString());
    parameters.replace("after", after.toString());

    return logicalMeterUseCases.findOneBy(parameters)
      .map(LogicalMeterDtoMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @PostMapping("{id}/synchronize")
  public ResponseEntity<Void> synchronizeMeter(@PathVariable UUID id) {
    LogicalMeter logicalMeter = logicalMeterUseCases.findById(id)
      .orElseThrow(() -> new MeterNotFound(id));

    meteringRequestPublisher.request(logicalMeter);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping
  public org.springframework.data.domain.Page<PagedLogicalMeterDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAll(parameters, adapter);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(LogicalMeterDtoMapper::toPagedDto);
  }

  @DeleteMapping("{id}")
  public LogicalMeterDto deleteMeter(@PathVariable UUID id) {
    return LogicalMeterDtoMapper.toDto(
      logicalMeterUseCases.deleteById(id)
        .orElseThrow(() -> new MeterNotFound(id))
    );
  }
}
