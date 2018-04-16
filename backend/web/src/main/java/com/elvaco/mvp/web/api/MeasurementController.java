package com.elvaco.mvp.web.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.util.ResolutionHelper;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import com.elvaco.mvp.web.exception.QuantityNotFound;
import com.elvaco.mvp.web.mapper.LabeledMeasurementValue;
import com.elvaco.mvp.web.mapper.MeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.util.LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeters;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeasurementMapper measurementMapper;

  @Autowired
  MeasurementController(
    MeasurementUseCases measurementUseCases,
    LogicalMeterUseCases logicalMeterUseCases,
    MeasurementMapper measurementMapper
  ) {
    this.measurementUseCases = measurementUseCases;
    this.logicalMeterUseCases = logicalMeterUseCases;
    this.measurementMapper = measurementMapper;
  }

  @GetMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return measurementUseCases.findById(id)
      .map(measurementMapper::toDto)
      .orElseThrow(() -> new MeasurementNotFound(id));
  }

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") List<String> quantityUnits,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    List<LogicalMeter> logicalMeters = getLogicalMetersByIds(meters);
    Set<Quantity> quantities = getQuantitiesFromQuantityUnitList(quantityUnits);

    if (quantities.isEmpty()) {
      throw new QuantityNotFound(quantityUnits.get(0));
    }

    if (before == null) {
      before = ZonedDateTime.now();
    }

    if (resolution == null) {
      resolution = ResolutionHelper.defaultResolutionFor(Duration.between(after, before));
    }

    Map<Quantity, List<PhysicalMeter>> quantityToPhysicalMeterIdMap =
      mapMeterQuantitiesToPhysicalMeters(
        logicalMeters,
        quantities
      );

    if (quantityToPhysicalMeterIdMap.values().stream().allMatch(List::isEmpty)) {
      throw new QuantityNotFound(quantities.stream().findAny().get().name);
    }

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();
    for (Map.Entry<Quantity, List<PhysicalMeter>> entry : quantityToPhysicalMeterIdMap.entrySet()) {
      Quantity quantity = entry.getKey();
      foundMeasurements.addAll(measurementUseCases.averageForPeriod(
        entry.getValue().stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
        quantity.name,
        quantity.defaultPresentationUnit(),
        after,
        before,
        resolution
      ).stream().map(measurementValue -> new LabeledMeasurementValue(
        "average",
        measurementValue.when,
        measurementValue.value,
        quantity
      )).collect(toList()));
    }

    return measurementMapper.toSeries(foundMeasurements);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") Optional<List<String>> quantityUnits,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before
  ) {
    //TODO: We need to limit the amount of measurements here. Even if we're only fetching
    // measurements for one meter, we might be fetching them over long period. E.g, measurements
    // for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
    // measurements, which is a bit too much.
    List<LogicalMeter> logicalMeters = getLogicalMetersByIds(meters);

    if (before == null) {
      before = ZonedDateTime.now();
    }

    if (after == null) {
      after = ZonedDateTime.parse("1970-01-01T00:00:00Z");
    }

    Set<Quantity> quantities = quantityUnits.map(this::getQuantitiesFromQuantityUnitList)
      .orElseGet(() -> logicalMeters.stream()
        .flatMap(logicalMeter -> logicalMeter.getQuantities().stream())
        .collect(toSet()));

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();
    Set<Map.Entry<Quantity, List<PhysicalMeter>>> entries = mapMeterQuantitiesToPhysicalMeters(
      logicalMeters,
      quantities
    ).entrySet();
    for (Map.Entry<Quantity, List<PhysicalMeter>> entry : entries) {
      for (PhysicalMeter meter : entry.getValue()) {
        List<MeasurementValue> series = measurementUseCases.seriesForPeriod(
          meter.id,
          entry.getKey().name,
          entry.getKey().defaultPresentationUnit(),
          entry.getKey().defaultSeriesDisplayMode(),
          after,
          before
        );
        foundMeasurements.addAll(series.stream()
          .map(measurementValue -> new LabeledMeasurementValue(
            meter.externalId,
            measurementValue.when,
            measurementValue.value,
            entry.getKey()
          )).collect(toList()));
      }
    }
    return measurementMapper.toSeries(foundMeasurements);
  }

  private Set<Quantity> getQuantitiesFromQuantityUnitList(List<String> quantityAndUnitList) {
    return quantityAndUnitList.stream().map(Quantity::of).collect(toSet());
  }

  private List<LogicalMeter> getLogicalMetersByIds(List<UUID> meters) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll("id", meters.stream()
        .map(UUID::toString)
        .collect(toList())
      );
    return logicalMeterUseCases.findAll(parameters);
  }
}
