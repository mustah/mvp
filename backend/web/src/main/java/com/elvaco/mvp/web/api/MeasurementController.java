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
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.exception.QuantityNotFound;
import com.elvaco.mvp.web.mapper.LabeledMeasurementValue;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeters;
import static com.elvaco.mvp.web.mapper.MeasurementDtoMapper.toSeries;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RequiredArgsConstructor
@RestApi("/api/v1/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping("/average")
  public List<MeasurementSeriesDto> average(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") List<String> quantityUnits,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution,
    @RequestParam(required = false, defaultValue = "average") String label
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

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();
    for (Map.Entry<Quantity, List<PhysicalMeter>> entry : quantityToPhysicalMeterIdMap.entrySet()) {
      Quantity quantity = entry.getKey();
      foundMeasurements.addAll(measurementUseCases.averageForPeriod(
        entry.getValue().stream().map(physicalMeter -> physicalMeter.id).collect(toList()),
        quantity,
        after,
        before,
        resolution
      ).stream().map(measurementValue -> new LabeledMeasurementValue(
        "average",
        label,
        measurementValue.when,
        measurementValue.value,
        quantity
      )).collect(toList()));
    }

    return toSeries(foundMeasurements);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @GetMapping
  public List<MeasurementSeriesDto> measurements(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantities") Optional<List<String>> quantityUnits,
    @RequestParam(defaultValue = "1970-01-01T00:00:00Z") @DateTimeFormat(iso = DATE_TIME)
      ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before,
    @RequestParam(required = false) TemporalResolution resolution
  ) {
    //TODO: We need to limit the amount of measurements here. Even if we're only fetching
    // measurements for one meter, we might be fetching them over long period. E.g, measurements
    // for one quantity for a meter with hour interval with 10 years of data = 365 * 10 * 24 = 87600
    // measurements, which is a bit too much.
    List<LogicalMeter> logicalMeters = getLogicalMetersByIds(meters).stream().collect(toList());
    Map<UUID, LogicalMeter> logicalMetersMap = logicalMeters.stream()
      .collect(toMap(LogicalMeter::getId, Function.identity()));

    if (before == null) {
      before = ZonedDateTime.now();
    }

    if (resolution == null) {
      resolution = ResolutionHelper.defaultResolutionFor(Duration.between(after, before));
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
          entry.getKey(),
          after,
          before,
          resolution
        );

        LogicalMeter logicalMeter = logicalMetersMap.get(meter.logicalMeterId);
        foundMeasurements.addAll(series.stream()
          .map(measurementValue -> new LabeledMeasurementValue(
            meter.logicalMeterId.toString(),
            meter.externalId,
            logicalMeter.location.getCity(),
            logicalMeter.location.getAddress(),
            logicalMeter.meterDefinition.medium,
            measurementValue.when,
            measurementValue.value,
            entry.getKey()
          )).collect(toList()));
      }
    }
    return toSeries(foundMeasurements);
  }

  private Set<Quantity> getQuantitiesFromQuantityUnitList(List<String> quantityAndUnitList) {
    return quantityAndUnitList.stream().map(Quantity::of).collect(toSet());
  }

  private List<LogicalMeter> getLogicalMetersByIds(List<UUID> meters) {
    RequestParameters parameters = new RequestParametersAdapter().setAll(
      ID,
      meters.stream().map(UUID::toString).collect(toList())
    );
    return logicalMeterUseCases.findAllWithStatuses(parameters);
  }
}
