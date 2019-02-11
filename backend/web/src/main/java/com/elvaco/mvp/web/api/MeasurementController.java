package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
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
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "quantity") Set<QuantityParameter> optionalQuantityParameters,
    @RequestParam(name = "after") @DateTimeFormat(iso = DATE_TIME) ZonedDateTime start,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);

    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAllBy(parameters);

    Map<String, QuantityParameter> quantityMap = getMappedQuantities(
      optionalQuantityParameters,
      logicalMeters
    );

    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(start, stop, resolution);

    var parameter = new MeasurementParameter(
      logicalMeters.stream().map(l -> l.id).collect(toList()),
      new ArrayList<>(quantityMap.values()),
      start,
      stop,
      temporalResolution
    );

    return measurementUseCases.findAverageForPeriod(parameter)
      .entrySet().stream().map(entry ->
        toSeries(
          entry.getValue(),
          label,
          String.format("average-%s", entry.getKey()),
          singleCityOrNull(parameters),
          quantityMap.get(entry.getKey())
        ))
      .collect(toList());
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam(name = "logicalMeterId") List<UUID> logicalMeterIds,
    @RequestParam(name = "quantity") Optional<Set<QuantityParameter>> optionalQuantityParameters,
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

    Map<String, QuantityParameter> quantityMap = getMappedQuantities(
      optionalQuantityParameters.orElse(emptySet()),
      logicalMeters
    );

    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(start, stop, resolution);

    MeasurementParameter parameter = new MeasurementParameter(
      logicalMeterIds,
      new ArrayList<>(quantityMap.values()),
      start,
      stop,
      temporalResolution
    );

    return measurementUseCases.findSeriesForPeriod(parameter)
      .entrySet().stream().map(entry ->
        toSeries(
          entry.getValue(),
          logicalMetersMap.get(entry.getKey().logicalMeterId),
          entry.getKey().physicalMeterAddress,
          quantityMap.get(entry.getKey().quantity)
        )
      ).collect(toList());
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

  private Map<String, QuantityParameter> getMappedQuantities(
    Set<QuantityParameter> quantityParameters,
    List<LogicalMeter> logicalMeters
  ) {
    Collector<QuantityParameter, ?, Map<String, QuantityParameter>> quantityParameterMapCollector =
      toMap(qParam -> qParam.name, qParam -> qParam);

    Map<String, QuantityParameter> meterQuantitites = logicalMeters.stream()
      .flatMap(logicalMeter -> logicalMeter.getQuantities()
        .stream()
        .map(QuantityParameter::of))
      .collect(Collectors.toSet())
      .stream()
      .collect(quantityParameterMapCollector);

    if (quantityParameters.isEmpty()) {
      return meterQuantitites;
    }

    return quantityParameters.stream()
      .filter(qp -> meterQuantitites.containsKey(qp.name))
      .map(qp -> {
        var meterQuantity = meterQuantitites.get(qp.name);
        return QuantityParameter.builder()
          .name(qp.name)
          .displayMode(qp.displayMode != null ? qp.displayMode : meterQuantity.displayMode)
          .unit(qp.unit != null ? qp.unit : meterQuantity.unit)
          .build();
      })
      .collect(quantityParameterMapCollector);
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

  @SafeVarargs
  private static <T> T coalesce(T... things) {
    return Arrays.stream(things).filter(Objects::nonNull).findFirst().orElse(null);
  }
}
