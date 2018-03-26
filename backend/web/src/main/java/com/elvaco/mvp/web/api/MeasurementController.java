package com.elvaco.mvp.web.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import com.elvaco.mvp.web.exception.NoPhysicalMetersException;
import com.elvaco.mvp.web.exception.QuantityNotFound;
import com.elvaco.mvp.web.mapper.LabeledMeasurementValue;
import com.elvaco.mvp.web.mapper.MeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime from,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime to,
    @RequestParam TemporalResolution resolution
  ) {
    List<LogicalMeter> logicalMeters = getLogicalMetersByIdList(meters);
    Set<Quantity> quantities = getQuantitiesFromQuantityUnitList(quantityUnits);

    if (quantities.isEmpty()) {
      throw new QuantityNotFound(quantityUnits.get(0));
    }

    if (to == null) {
      to = ZonedDateTime.now();
    }

    Map<Quantity, List<UUID>> quantityToPhysicalMeterIdMap = LogicalMeterHelper
      .mapMeterQuantitiesToPhysicalMeterUuids(
        logicalMeters,
        quantities
      );

    if (quantityToPhysicalMeterIdMap.values().stream().allMatch(List::isEmpty)) {
      throw new NoPhysicalMetersException();
    }

    List<LabeledMeasurementValue> foundMeasurements = new ArrayList<>();
    for (Map.Entry<Quantity, List<UUID>> entry : quantityToPhysicalMeterIdMap.entrySet()) {
      Quantity quantity = entry.getKey();
      foundMeasurements.addAll(measurementUseCases.averageForPeriod(
        entry.getValue(),
        quantity.name,
        quantity.unit,
        from,
        to,
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
    @RequestParam(name = "quantities") Optional<List<String>> quantityUnits,
    @RequestParam Optional<List<UUID>> meters,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime after,
    @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime before

  ) {
    List<LogicalMeter> logicalMeters =
      meters.map(this::getLogicalMetersByIdList).orElseGet(logicalMeterUseCases::findAll);

    Set<Quantity> quantities = quantityUnits.map(this::getQuantitiesFromQuantityUnitList)
      .orElseGet(() -> logicalMeters
        .stream()
        .flatMap(
          logicalMeter -> logicalMeter.getQuantities().stream()
        )
        .collect(toSet()));

    Map<Quantity, List<UUID>> quantityToPhysicalMeterIdMap = LogicalMeterHelper
      .mapMeterQuantitiesToPhysicalMeterUuids(
        logicalMeters,
        quantities
      );

    List<Measurement> foundMeasurements = new ArrayList<>();
    for (Map.Entry<Quantity, List<UUID>> entry : quantityToPhysicalMeterIdMap.entrySet()) {
      RequestParameters requestParams = new RequestParametersAdapter();
      requestParams.setAll(
        "meterId",
        entry.getValue()
          .stream()
          .map(UUID::toString)
          .collect(toList())
      );
      requestParams.add("quantity", entry.getKey().name);
      if (after != null) {
        requestParams.add("after", after.toString());
      }

      if (before != null) {
        requestParams.add("before", before.toString());
      }

      foundMeasurements.addAll(
        measurementUseCases.findAll(entry.getKey().unit, requestParams)
      );
    }

    return measurementMapper.toSeries(foundMeasurements.stream()
                                        .map(LabeledMeasurementValue::from)
                                        .collect(toList()));
  }

  private Set<Quantity> getQuantitiesFromQuantityUnitList(List<String> quantityAndUnitList) {
    return quantityAndUnitList.stream().map(Quantity::of).collect(toSet());
  }

  private List<LogicalMeter> getLogicalMetersByIdList(@RequestParam List<UUID> meters) {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll("id", meters.stream()
        .map(UUID::toString)
        .collect(toList())
      );
    return logicalMeterUseCases.findAll(parameters);
  }
}
