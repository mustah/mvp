package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

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
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;
import com.elvaco.mvp.web.mapper.LabeledMeasurementValue;
import com.elvaco.mvp.web.mapper.MeasurementDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.groupByQuantity;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeters;
import static com.elvaco.mvp.core.util.QuantityHelper.complementWithUnits;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.Collections.emptyList;
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

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") Set<Quantity> quantities,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
  ) {
    ZonedDateTime stop = beforeOrNow(before);
    return measurementSeriesOf(
      after,
      stop,
      resolutionOrDefault(after, stop, resolution),
      quantities,
      findLogicalMetersByIds(meters),
      (quantity, measurementValue) -> new LabeledMeasurementValue(
        String.format("average-%s", quantity.name),
        label,
        measurementValue.when,
        measurementValue.value,
        quantity
      )
    );
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") Optional<Set<Quantity>> maybeQuantities,
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
    List<LogicalMeter> logicalMeters = findLogicalMetersByIds(meters);
    Map<UUID, LogicalMeter> logicalMetersMap = logicalMeters.stream()
      .collect(toMap(LogicalMeter::getId, Function.identity()));

    Set<Quantity> quantities = maybeQuantities
      .orElseGet(() -> logicalMeters.stream()
        .flatMap(logicalMeter -> logicalMeter.getQuantities().stream())
        .collect(toSet()));

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();

    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(after, stop, resolution);

    mapMeterQuantitiesToPhysicalMeters(logicalMeters, quantities)
      .forEach((quantity, physicalMeters) -> {
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
        });
      });

    return toSeries(foundMeasurements);
  }

  @GetMapping("/cities")
  public List<MeasurementSeriesDto> cities(
    @RequestParam(name = "city") List<CityDto> cities,
    @RequestParam(name = "quantities") Set<Quantity> quantities,
    @RequestParam(defaultValue = "1970-01-01T00:00:00Z") @DateTimeFormat(iso = DATE_TIME)
      ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    ZonedDateTime stop = beforeOrNow(before);
    TemporalResolution temporalResolution = resolutionOrDefault(after, stop, resolution);
    Set<Quantity> complementedQuantities = complementWithUnits(quantities);

    return cities.stream()
      .flatMap((city) -> {
        String cityId = String.format("%s,%s", city.country, city.name);
        return measurementSeriesOfCity(
          after,
          stop,
          temporalResolution,
          complementedQuantities,
          findLogicalMetersByCityId(cityId),
          (quantity, measurementValue) -> new LabeledMeasurementValue(
            String.format("city-%s,%s-%s", city.country, city.name, quantity.name),
            cityId,
            city.name,
            null,
            null,
            measurementValue.when,
            measurementValue.value,
            quantity
          )
        ).stream();
      })
      .collect(toList());
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

  private List<MeasurementSeriesDto> measurementSeriesOf(
    ZonedDateTime after,
    ZonedDateTime before,
    TemporalResolution resolution,
    Set<Quantity> quantities,
    List<LogicalMeter> logicalMeters,
    BiFunction<Quantity, MeasurementValue, LabeledMeasurementValue> valueMapper
  ) {
    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();

    mapMeterQuantitiesToPhysicalMeters(logicalMeters, quantities)
      .forEach((quantity, physicalMeters) -> foundMeasurements.addAll(
        measurementUseCases.averageForPeriod(
          physicalMeters.stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
          quantity,
          after,
          before,
          resolution
        ).stream()
          .map((measurementValue) -> valueMapper.apply(quantity, measurementValue))
          .collect(toList())
      ));

    return toSeries(foundMeasurements);
  }

  private List<MeasurementSeriesDto> measurementSeriesOfCity(
    ZonedDateTime after,
    ZonedDateTime before,
    TemporalResolution resolution,
    Set<Quantity> quantities,
    List<LogicalMeter> logicalMeters,
    BiFunction<Quantity, MeasurementValue, LabeledMeasurementValue> valueMapper
  ) {
    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();

    groupByQuantity(logicalMeters, quantities)
      .forEach((quantity, physicalMeters) -> foundMeasurements.addAll(
        measurementUseCases.averageForPeriod(
          physicalMeters.stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
          quantity,
          after,
          before,
          resolution
        ).stream()
          .map((measurementValue) -> valueMapper.apply(quantity, measurementValue))
          .collect(toList())
      ));

    return toSeries(foundMeasurements);
  }

  private List<LogicalMeter> findLogicalMetersByIds(List<UUID> logicalMeterIds) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll(ID, logicalMeterIds.stream().map(UUID::toString).collect(toList()));
    return logicalMeterUseCases.findAllBy(parameters);
  }

  private List<LogicalMeter> findLogicalMetersByCityId(String cityId) {
    RequestParameters parameters = new RequestParametersAdapter().add(CITY, cityId);
    return logicalMeterUseCases.findAllBy(parameters);
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
