package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementRequestDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;

  @PostMapping
  public List<MeasurementSeriesDto> measurementsPost(
    @JsonProperty @RequestBody MeasurementRequestDto requestBody
  ) {
    return measurements(
      toLogicalMeterIdParameters(requestBody.logicalMeterId),
      toQuantityParameters(requestBody.quantity),
      requestBody.reportAfter,
      requestBody.reportBefore,
      requestBody.resolution
    );
  }

  @PostMapping("/average")
  public List<MeasurementSeriesDto> averagePost(
    @JsonProperty @RequestBody MeasurementRequestDto requestBody
  ) {
    return average(
      toLogicalMeterIdParameters(requestBody.logicalMeterId),
      toQuantityParameters(requestBody.quantity),
      requestBody.reportAfter,
      requestBody.reportBefore,
      requestBody.resolution,
      requestBody.label
    );
  }

  @GetMapping("/average")
  public List<MeasurementSeriesDto> averageGet(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "quantity") Set<QuantityParameter> quantityParameters,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportAfter,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportBefore,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    return average(
      RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID),
      quantityParameters,
      reportAfter,
      reportBefore,
      resolution,
      label
    );
  }

  @GetMapping
  public List<MeasurementSeriesDto> measurementsGet(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam(name = "quantity") Optional<Set<QuantityParameter>> optionalQuantityParameters,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportAfter,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime reportBefore,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    return measurements(
      RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID),
      optionalQuantityParameters.orElse(emptySet()),
      reportAfter,
      reportBefore,
      resolution
    );
  }

  private List<MeasurementSeriesDto> average(
    RequestParameters parameters,
    Set<QuantityParameter> quantities,
    ZonedDateTime reportStart,
    ZonedDateTime reportBefore,
    TemporalResolution resolution,
    String label
  ) {
    Map<String, QuantityParameter> quantityMap = getMappedQuantities(quantities, parameters);

    ZonedDateTime reportStop = beforeOrNow(reportBefore);

    var parameter = new MeasurementParameter(
      parameters.setReportPeriod(reportStart, reportStop),
      new ArrayList<>(quantityMap.values()),
      reportStart,
      reportStop,
      resolutionOrDefault(reportStart, reportStop, resolution)
    );

    return measurementUseCases.findAverageForPeriod(parameter)
      .entrySet().stream()
      .map(entry -> toSeries(
        entry.getValue(),
        label,
        String.format("average-%s", entry.getKey()),
        quantityMap.get(entry.getKey())
      ))
      .collect(toList());
  }

  /**
   * We need to limit the amount of measurements here. Even if we're only fetching
   * measurements for one meter, we might be fetching them over long period. E.g, measurements
   * for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
   * measurements, which is a bit too much.
   */
  private List<MeasurementSeriesDto> measurements(
    RequestParameters parameters,
    Set<QuantityParameter> quantities,
    ZonedDateTime reportStart,
    @Nullable ZonedDateTime before,
    @Nullable TemporalResolution resolution
  ) {
    Map<String, QuantityParameter> quantityMap = getMappedQuantities(quantities, parameters);

    ZonedDateTime reportStop = beforeOrNow(before);

    MeasurementParameter parameter = new MeasurementParameter(
      parameters.setReportPeriod(reportStart, reportStop),
      new ArrayList<>(quantityMap.values()),
      reportStart,
      reportStop,
      resolutionOrDefault(reportStart, reportStop, resolution)
    );

    return measurementUseCases.findSeriesForPeriod(parameter)
      .entrySet().stream()
      .map(entry -> toSeries(
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
    } else {
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
  }

  private static RequestParameters toLogicalMeterIdParameters(List<String> logicalMeterId) {
    return RequestParametersAdapter.of().setAll(LOGICAL_METER_ID, logicalMeterId);
  }

  private static Set<QuantityParameter> toQuantityParameters(Set<String> quantities) {
    return Optional.ofNullable(quantities).orElse(emptySet()).stream()
      .map(QuantityParameter::of)
      .collect(toUnmodifiableSet());
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
