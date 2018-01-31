package com.elvaco.mvp.api;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.FilterToPredicateMapper;
import com.querydsl.core.types.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.util.ParametersHelper.combineParams;

@RestApi("/v1/api/physical-meters")
@ExposesResourceFor(PhysicalMeter.class)
public class PhysicalMeterController {

  private final MeasurementJpaRepository measurementRepository;
  private final ModelMapper modelMapper;
  private final FilterToPredicateMapper predicateMapper;

  @Autowired
  public PhysicalMeterController(
    MeasurementJpaRepository measurementRepository,
    ModelMapper modelMapper,
    FilterToPredicateMapper predicateMapper
  ) {
    this.measurementRepository = measurementRepository;
    this.modelMapper = modelMapper;
    this.predicateMapper = predicateMapper;
  }

  @GetMapping("{meterId}/measurements/{quantity}")
  public Page<MeasurementDto> measurementsByQuantityForPhysicalMeter(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterMeasurementDtos(combineParams(pathVars, requestParams), pageable);
  }

  @GetMapping("{meterId}/measurements")
  public Page<MeasurementDto> measurementsForPhysicalMeter(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterMeasurementDtos(combineParams(pathVars, requestParams), pageable);
  }

  private Page<MeasurementDto> filterMeasurementDtos(
    Map<String, List<String>> filter,
    Pageable pageable
  ) {
    Predicate predicate = predicateMapper.map(filter);
    return measurementRepository
      .findAll(predicate, pageable)
      .map(source -> modelMapper.map(source, MeasurementDto.class));
  }
}
