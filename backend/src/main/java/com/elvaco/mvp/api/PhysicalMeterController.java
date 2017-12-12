package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import com.querydsl.core.types.Predicate;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestApi
@ExposesResourceFor(PhysicalMeterEntity.class)
@RequestMapping("/api/physical-meters")
public class PhysicalMeterController {
  private final PhysicalMeterRepository repository;
  private final MeasurementRepository measurementRepository;
  private final ModelMapper modelMapper;

  @Autowired
  public PhysicalMeterController(PhysicalMeterRepository repository,
                                 MeasurementRepository measurementRepository,
                                 ModelMapper modelMapper) {
    this.repository = repository;
    this.measurementRepository = measurementRepository;
    this.modelMapper = modelMapper;
  }

  @RequestMapping("{id}")
  public PhysicalMeterEntity physicalMeter(@PathVariable("id") Long id) {
    return repository.findOne(id);
  }

  @RequestMapping("{meterId}/measurements/{quantity}")
  public Page<MeasurementDto> measurementsByQuantityForPhysicalMeter(
      @PathVariable Map<String, String> pathVars,
      @RequestParam MultiValueMap<String, String> requestParams,
      Pageable pageable) {

    return filterMeasurementDtos(
        combineParams(pathVars, requestParams),
        pageable);
  }

  @RequestMapping("{meterId}/measurements")
  public Page<MeasurementDto> measurementsForPhysicalMeter(
      @PathVariable Map<String, String> pathVars,
      @RequestParam MultiValueMap<String, String> requestParams,
      Pageable pageable) {
    return filterMeasurementDtos(
        combineParams(pathVars, requestParams),
        pageable);
  }

  @RequestMapping("")
  public Page<PhysicalMeterEntity> physicalMeters(Pageable pageable) {
    return repository.findAll(pageable);
  }

  private Page<MeasurementDto> filterMeasurementDtos(Map<String, List<String>> filter,
                                                     Pageable pageable) {
    MeasurementFilterToPredicateMapper filterToPredicateTranslator =
        new MeasurementFilterToPredicateMapper();
    Predicate predicate = filterToPredicateTranslator.map(filter);
    return measurementRepository
        .findAll(predicate, pageable)
        .map(source -> modelMapper.map(source, MeasurementDto.class));
  }

  private Map<String, List<String>> combineParams(Map<String, String> pathVars,
                                                  Map<String, List<String>> requestParams) {
    Map<String, List<String>> filter = new HashMap<>();
    filter.putAll(requestParams);
    filter.putAll(
        pathVars
            .entrySet()
            .stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), Arrays.asList(e.getValue()))
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
    return filter;
  }

}
