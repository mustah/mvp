package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeasurementMessageResponseBuilder;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.util.CompletenessValidators;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageHelper.removeSimultaneousQuantityValues;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mappedQuantityName;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMeterDefinition;
import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class MeteringMeasurementMessageConsumer implements MeasurementMessageConsumer {

  /**
   * Metering stores and treats all values as CET.
   * At least it's consistent!
   */
  static final ZoneId METERING_TIMEZONE = ZoneId.of("CET");

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;

  @Override
  public Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto measurementMessage) {
    String facilityId = measurementMessage.facility.id;
    if (facilityId.trim().isEmpty()) {
      log.warn("Discarding measurement message with invalid facility/external ID '{}'", facilityId);
      return Optional.empty();
    }

    Organisation organisation =
      organisationUseCases.findOrCreate(measurementMessage.organisationId);

    MeasurementMessageResponseBuilder responseBuilder =
      new MeasurementMessageResponseBuilder(measurementMessage.organisationId);

    LogicalMeter logicalMeter =
      logicalMeterUseCases.findByOrganisationIdAndExternalId(
        organisation.id,
        facilityId
      ).orElseGet(() -> {
        Medium medium = Medium.from(resolveMeterDefinition(measurementMessage.values).medium);
        return new LogicalMeter(
          randomUUID(),
          facilityId,
          organisation.id,
          MeterDefinition.fromMedium(medium),
          UNKNOWN_LOCATION
        );
      });

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByOrganisationIdAndExternalIdAndAddress(
        organisation.id,
        facilityId,
        measurementMessage.meter.id
      ).orElseGet(() -> PhysicalMeter.builder()
        .organisation(organisation)
        .address(measurementMessage.meter.id)
        .externalId(facilityId)
        .medium(UNKNOWN_MEDIUM.medium)
        .logicalMeterId(logicalMeter.id)
        .readIntervalMinutes(0)
        .build());

    if (CompletenessValidators.physicalMeter().isIncomplete(physicalMeter)
      || CompletenessValidators.logicalMeter().isIncomplete(logicalMeter)) {
      responseBuilder.setFacilityId(facilityId);
      responseBuilder.setMeterExternalId(measurementMessage.meter.id);
    }

    Optional<Gateway> optionalGateway = Optional.empty();
    if (measurementMessage.gateway().isPresent()) {
      GatewayIdDto gatewayId = measurementMessage.gateway().get();
      optionalGateway = Optional.of(gatewayUseCases.findBy(
        organisation.id,
        gatewayId.id
      ).orElseGet(() -> new Gateway(
        randomUUID(),
        organisation.id,
        gatewayId.id,
        "",
        singletonList(logicalMeter),
        emptyList() // TODO Save gateway status
      )));

      if (CompletenessValidators.gateway().isIncomplete(optionalGateway.get())) {
        responseBuilder.setFacilityId(facilityId);
        responseBuilder.setGatewayExternalId(gatewayId.id);
      }

    }

    List<Measurement> measurements = removeSimultaneousQuantityValues(measurementMessage.values)
      .stream()
      .map(value -> measurementUseCases
        .findForMeterCreatedAt(
          physicalMeter.id,
          mappedQuantityName(value.quantity),
          value.timestamp.atZone(METERING_TIMEZONE)
        ).orElseGet(() ->
          new Measurement(
            null,
            value.timestamp.atZone(METERING_TIMEZONE),
            mappedQuantityName(value.quantity),
            value.value,
            value.unit,
            physicalMeter
          )
        ).withValue(value.value)
        .withUnit(value.unit)
        .withQuantity(mappedQuantityName(value.quantity))
      )
      .collect(toList());

    if (optionalGateway.isPresent()) {
      gatewayUseCases.save(optionalGateway.get());
      logicalMeterUseCases.save(
        logicalMeter
          .withGateway(optionalGateway.get())
          .withPhysicalMeter(physicalMeter));
    } else {
      logicalMeterUseCases.save(logicalMeter.withPhysicalMeter(physicalMeter));
    }

    physicalMeterUseCases.save(physicalMeter);
    measurementUseCases.save(measurements);

    return responseBuilder.build();
  }
}
