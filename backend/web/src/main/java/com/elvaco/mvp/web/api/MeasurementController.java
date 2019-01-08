package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.exception.MissingParameter;
import com.elvaco.mvp.web.mapper.LabeledMeasurementValue;
import com.elvaco.mvp.web.mapper.MeasurementDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final LogicalMeterHelper logicalMeterHelper;

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "after") @DateTimeFormat(iso = DATE_TIME) ZonedDateTime start,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);

    Set<Quantity> quantities = parameters.getValues(QUANTITY).stream()
      .map(Quantity::of)
      .collect(toSet());

    if (quantities.isEmpty()) {
      throw new MissingParameter(QUANTITY);
    }

    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAllBy(parameters);

    ZonedDateTime stop = beforeOrNow(before);

    return toSeries(logicalMeterHelper.groupByQuantity(logicalMeters, quantities)
      .entrySet().stream()
      .flatMap(entry -> measurementUseCases.findAverageForPeriod(
        new MeasurementParameter(
          entry.getValue().stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
          entry.getKey(),
          start,
          stop,
          resolutionOrDefault(start, stop, resolution)
        ))
        .stream()
        .map(measurementValue -> LabeledMeasurementValue.builder()
          .id(String.format("average-%s", entry.getKey().name))
          .label(label)
          .when(measurementValue.when)
          .value(measurementValue.value)
          .quantity(entry.getKey())
          .city(singleCityOrNull(parameters))
          .build()
        ))
      .collect(toList())
    );
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam(name = "logicalMeterId") List<UUID> logicalMeterIds,
    @RequestParam(name = "quantity") Optional<Set<Quantity>> optionalQuantities,
    @RequestParam(name = "after") @DateTimeFormat(iso = DATE_TIME) ZonedDateTime start,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    // TODO: We need to limit the amount of measurements here. Even if we're only fetching
    // measurements for one meter, we might be fetching them over long period. E.g, measurements
    // for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
    // measurements, which is a bit too much.
    List<LogicalMeter> logicalMeters = findLogicalMetersByIds(logicalMeterIds);

    Map<UUID, LogicalMeter> logicalMetersMap = logicalMeters.stream()
      .collect(toMap(LogicalMeter::getId, identity()));

    Set<Quantity> quantities = optionalQuantities
      .orElseGet(() -> logicalMeters.stream()
        .flatMap(logicalMeter -> logicalMeter.getQuantities().stream())
        .collect(toSet()));

    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(start, stop, resolution);

    Map<Quantity, List<PhysicalMeter>> quantityToPhysicalMeter
      = logicalMeterHelper.mapQuantitiesToPhysicalMeters(
      logicalMeters,
      quantities
    );

    Map<UUID, PhysicalMeter> physicalMeters = quantityToPhysicalMeter
      .values().stream().flatMap(Collection::stream)
      .collect(toMap(PhysicalMeter::getId, identity(), (o, n) -> n));

    List<LabeledMeasurementValue> labeledMeasurementValues =
      quantityToPhysicalMeter.entrySet()
        .stream()
        .map(entry -> new MeasurementParameter(
          entry.getValue().stream().map(PhysicalMeter::getId).collect(toList()),
          entry.getKey(),
          start,
          stop,
          temporalResolution
        ))
        .collect(toMap(MeasurementParameter::getQuantity, measurementUseCases::findSeriesForPeriod))
        .entrySet().stream()
        .flatMap(quantityMapEntry ->
          quantityMapEntry.getValue()
            .entrySet()
            .stream()
            .flatMap(uuidListEntry -> {
              PhysicalMeter physicalMeter = physicalMeters.get(uuidListEntry.getKey());
              LogicalMeter logicalMeter = logicalMetersMap.get(physicalMeter.logicalMeterId);
              return uuidListEntry.getValue()
                .stream().map(measurementValue ->
                  LabeledMeasurementValue.builder()
                    .id(logicalMeter.id.toString())
                    .label(logicalMeter.externalId)
                    .city(logicalMeter.location.getCity())
                    .address(logicalMeter.location.getAddress())
                    .medium(logicalMeter.meterDefinition.medium)
                    .when(measurementValue.when)
                    .value(measurementValue.value)
                    .quantity(quantityMapEntry.getKey())
                    .build()
                );
            })
        ).collect(toList());

    return toSeries(labeledMeasurementValues);
  }

  @GetMapping("/paged")
  public Page<MeasurementDto> latestMeasurements(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    var parameters = RequestParametersAdapter.of(requestParams);
    var measurements = measurementUseCases.findAll(parameters).stream()
      .map(MeasurementDtoMapper::toDto)
      .collect(toList());
    return new PageImpl<>(measurements);
  }

  private List<LogicalMeter> findLogicalMetersByIds(List<UUID> logicalMeterIds) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll(LOGICAL_METER_ID, logicalMeterIds.stream().map(UUID::toString).collect(toList()));
    return logicalMeterUseCases.findAllBy(parameters);
  }

  @Nullable
  private static String singleCityOrNull(RequestParameters requestParameters) {
    var cities = requestParameters.getValues(CITY);
    return cities.isEmpty() || cities.size() > 1 ? null : cities.iterator().next();
  }

  private static ZonedDateTime beforeOrNow(ZonedDateTime before) {
    return Optional.ofNullable(before).orElseGet(ZonedDateTime::now);
  }

  private static TemporalResolution resolutionOrDefault(
    ZonedDateTime start,
    ZonedDateTime stop,
    TemporalResolution resolution
  ) {
    return Optional.ofNullable(resolution)
      .orElseGet(() -> TemporalResolution.defaultResolutionFor(Duration.between(start, stop)));
  }
}
