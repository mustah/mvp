package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.Page;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private static final PageImpl<MeasurementDto> EMPTY_PAGE = new PageImpl<>(emptyList());

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final LogicalMeterHelper logicalMeterHelper;

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam MultiValueMap<String, String> requestParams,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    ZonedDateTime stop = beforeOrNow(before);
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);

    Set<Quantity> quantities = parameters.getValues(QUANTITY).stream()
      .map(Quantity::of)
      .collect(toSet());

    if (quantities.isEmpty()) {
      throw new MissingParameter(QUANTITY);
    }

    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAllBy(parameters);
    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();

    logicalMeterHelper.groupByQuantity(logicalMeters, quantities)
      .forEach((quantity, physicalMeters) -> foundMeasurements.addAll(
        measurementUseCases.averageForPeriod(
          physicalMeters.stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
          quantity,
          after,
          stop,
          resolutionOrDefault(after, stop, resolution)
        ).stream()
          .map((measurementValue) -> LabeledMeasurementValue.builder()
            .id(String.format("average-%s", quantity.name))
            .label(label)
            .when(measurementValue.when)
            .value(measurementValue.value)
            .quantity(quantity)
            .city(singleCityOrNull(parameters))
            .build()
          )
          .collect(toList())
      ));

    return toSeries(foundMeasurements);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam List<UUID> logicalMeterId,
    @RequestParam(name = "quantity") Optional<Set<Quantity>> maybeQuantities,
    @RequestParam(defaultValue = "1970-01-01T00:00:00Z")
    @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false)
    @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    // TODO: We need to limit the amount of measurements here. Even if we're only fetching
    // measurements for one meter, we might be fetching them over long period. E.g, measurements
    // for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
    // measurements, which is a bit too much.
    List<LogicalMeter> logicalMeters = findLogicalMetersByIds(logicalMeterId);
    Map<UUID, LogicalMeter> logicalMetersMap = logicalMeters.stream()
      .collect(toMap(LogicalMeter::getId, identity()));

    Set<Quantity> quantities = maybeQuantities
      .orElseGet(() -> logicalMeters.stream()
        .flatMap(logicalMeter -> logicalMeter.getQuantities().stream())
        .collect(toSet()));

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();

    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(after, stop, resolution);

    logicalMeterHelper.mapMeterQuantitiesToPhysicalMeters(logicalMeters, quantities)
      .forEach((quantity, physicalMeters) ->
        physicalMeters.forEach((physicalMeter) -> {
          List<MeasurementValue> series = measurementUseCases.seriesForPeriod(
            physicalMeter.id,
            quantity,
            after,
            stop,
            temporalResolution
          );

          LogicalMeter logicalMeter = logicalMetersMap.get(physicalMeter.logicalMeterId);
          foundMeasurements.addAll(series.stream()
            .map(measurementValue -> new LabeledMeasurementValue(
              physicalMeter.logicalMeterId.toString(),
              physicalMeter.externalId,
              logicalMeter.location.getCity(),
              logicalMeter.location.getAddress(),
              logicalMeter.meterDefinition.medium,
              measurementValue.when,
              measurementValue.value,
              quantity
            )).collect(toList()));
        }));

    return toSeries(foundMeasurements);
  }

  @GetMapping("/paged")
  public org.springframework.data.domain.Page<MeasurementDto> pagedMeasurements(
    @RequestParam UUID logicalMeterId,
    Pageable pageable
  ) {
    return logicalMeterUseCases.effectiveOrganisationId(logicalMeterId)
      .map(findMeasurements(logicalMeterId, new PageableAdapter(pageable)))
      .map(page -> new PageImpl<>(page.getContent(), pageable, page.getTotalElements()))
      .orElse(EMPTY_PAGE);
  }

  private Function<UUID, Page<MeasurementDto>> findMeasurements(
    UUID logicalMeterId,
    PageableAdapter pageable
  ) {
    return organisationId -> measurementUseCases.findAllBy(organisationId, logicalMeterId, pageable)
      .map(MeasurementDtoMapper::toDto);
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
