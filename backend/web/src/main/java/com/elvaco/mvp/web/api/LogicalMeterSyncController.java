package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.web.exception.MeterNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;

@RequiredArgsConstructor
@RestApi("/api/v1/meters/sync")
public class LogicalMeterSyncController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeteringRequestPublisher meteringRequestPublisher;
  private final PropertiesUseCases propertiesUseCases;

  @PostMapping("{id}")
  public ResponseEntity<Void> synchronizeMeter(@PathVariable UUID id) {
    logicalMeterUseCases.findById(id)
      .map(this::sync)
      .orElseThrow(() -> new MeterNotFound(id));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PostMapping
  public ResponseEntity<Void> synchronizeMetersByIds(@RequestBody List<UUID> logicalMetersIds) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAllIds(ID, logicalMetersIds);

    logicalMeterUseCases.findAllBy(parameters).forEach(this::sync);

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private LogicalMeter sync(LogicalMeter logicalMeter) {
    meteringRequestPublisher.request(logicalMeter);
    propertiesUseCases.forceUpdateGeolocation(logicalMeter.id, logicalMeter.organisationId);
    return logicalMeter;
  }
}
