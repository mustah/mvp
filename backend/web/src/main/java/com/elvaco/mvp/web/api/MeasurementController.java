package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "quantity") Set<QuantityParameter> optionalQuantityParameters,
    @RequestParam(name = "reportAfter") @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportStart,

    @RequestParam(required = false, name = "reportBefore")
    @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,

    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);

    Map<String, QuantityParameter> quantityMap = getMappedQuantities(
      optionalQuantityParameters,
      parameters
    );

    ZonedDateTime reportStop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(
      reportStart,
      reportStop,
      resolution
    );

    var parameter = new MeasurementParameter(
      parameters.setReportPeriod(reportStart, reportStop),
      new ArrayList<>(quantityMap.values()),
      reportStart,
      reportStop,
      temporalResolution
    );

    return measurementUseCases.findAverageForPeriod(parameter)
      .entrySet().stream()
      .map(entry ->
        toSeries(
          entry.getValue(),
          label,
          String.format("average-%s", entry.getKey()),
          quantityMap.get(entry.getKey())
        ))
      .collect(toList());
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "quantity") Optional<Set<QuantityParameter>> optionalQuantityParameters,
    @RequestParam(name = "reportAfter") @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportStart,

    @RequestParam(required = false, name = "reportBefore")
    @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,

    @RequestParam(required = false) TemporalResolution resolution
  ) {
    // TODO: We need to limit the amount of measurements here. Even if we're only fetching
    // measurements for one meter, we might be fetching them over long period. E.g, measurements
    // for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
    // measurements, which is a bit too much.
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);
    Map<String, QuantityParameter> quantityMap = getMappedQuantities(
      optionalQuantityParameters.orElse(emptySet()),
      parameters
    );

    ZonedDateTime reportStop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(
      reportStart,
      reportStop,
      resolution
    );

    MeasurementParameter parameter = new MeasurementParameter(
      parameters.setReportPeriod(reportStart, reportStop),
      new ArrayList<>(quantityMap.values()),
      reportStart,
      reportStop,
      temporalResolution
    );

    return measurementUseCases.findSeriesForPeriod(parameter)
      .entrySet().stream()
      .map(entry ->
        toSeries(
          entry.getValue(),
          entry.getKey().logicalMeterId,
          entry.getKey().externalId,
          entry.getKey().mediumName,
          entry.getKey().physicalMeterAddress,
          quantityMap.get(entry.getKey().quantity)
        ))
      .collect(toList());
  }

  private Map<String, QuantityParameter> getMappedQuantities(
    Set<QuantityParameter> quantityParameters,
    RequestParameters parameters
  ) {
    Map<String, QuantityParameter> preferredQuantityParameters =
      measurementUseCases.getPreferredQuantityParameters(parameters);

    if (quantityParameters.isEmpty()) {
      return preferredQuantityParameters;
    }

    return quantityParameters.stream()
      .filter(qp -> preferredQuantityParameters.containsKey(qp.name))
      .map(qp -> QuantityParameter.builder()
        .name(qp.name)
        .unit(qp.unit != null ? qp.unit : preferredQuantityParameters.get(qp.name).unit)
        .displayMode(qp.displayMode != null
          ? qp.displayMode
          : preferredQuantityParameters.get(qp.name).displayMode)
        .build())
      .collect(toMap(qp -> qp.name, qp -> qp));
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
