package com.elvaco.mvp.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.web.dto.SyncRequestResponseDto;
import com.elvaco.mvp.web.dto.SyncRequestStatusDto;
import com.elvaco.mvp.web.exception.MeterNotFound;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/meters/sync")
public class LogicalMeterSyncController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeteringRequestPublisher meteringRequestPublisher;
  private final PropertiesUseCases propertiesUseCases;
  private final JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;
  private final AuthenticatedUser authenticatedUser;

  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("{id}")
  public List<SyncRequestResponseDto> synchronizeMeter(@PathVariable UUID id) {
    return logicalMeterUseCases.findById(id)
      .map(this::sync)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping
  public List<SyncRequestResponseDto> synchronizeMetersByIds(
    @RequestBody List<UUID> logicalMetersIds
  ) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAllIds(LOGICAL_METER_ID, logicalMetersIds);

    return logicalMeterUseCases.findAllBy(parameters).stream()
      .flatMap(logicalMeter -> sync(logicalMeter).stream())
      .collect(toList());
  }

  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping(path = "/organisation")
  public List<SyncRequestResponseDto> synchronizeMetersOrganisationIds(
    @RequestParam UUID id
  ) {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized(String.format(
        "User '%s' is not allowed to publish synchronization requests",
        authenticatedUser.getUsername()
      ));
    }
    RequestParameters parameters = new RequestParametersAdapter()
      .add(RequestParameter.ORGANISATION, id.toString());

    return logicalMeterUseCases.findAllBy(parameters).stream()
      .flatMap(logicalMeter -> sync(logicalMeter).stream())
      .collect(toList());
  }

  @GetMapping
  public List<SyncRequestStatusDto> syncStatus(
    @RequestParam List<String> jobIds
  ) {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized(String.format(
        "User '%s' is not allowed to view synchronization requests",
        authenticatedUser.getUsername()
      ));
    }

    return jobIds.stream()
      .map(jobId -> SyncRequestStatusDto.from(jobId, meterSyncJobService.getJob(jobId)))
      .collect(toList());
  }

  private List<SyncRequestResponseDto> sync(LogicalMeter logicalMeter) {
    String jobId = meteringRequestPublisher.request(logicalMeter);
    propertiesUseCases.forceUpdateGeolocation(logicalMeter.id, logicalMeter.organisationId);
    List<SyncRequestResponseDto> result = new ArrayList<>();
    result.add(new SyncRequestResponseDto(logicalMeter.id, null, jobId));
    result.addAll(logicalMeter.gateways.stream().map(this::syncGateway).collect(toList()));
    return result;
  }

  private SyncRequestResponseDto syncGateway(Gateway gateway) {
    String jobId = meteringRequestPublisher.request(gateway);
    return new SyncRequestResponseDto(gateway.id, null, jobId);
  }
}
