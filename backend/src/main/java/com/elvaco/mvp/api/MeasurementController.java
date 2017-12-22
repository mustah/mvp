package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.repository.MeasurementRepository;

import com.querydsl.core.types.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestApi("/api/measurements")
public class MeasurementController {

  private final MeasurementRepository repository;
  private final ModelMapper modelMapper;
  private final MeasurementFilterToPredicateMapper predicateMapper;

  @Autowired
  MeasurementController(MeasurementRepository repository, ModelMapper modelMapper,
                        MeasurementFilterToPredicateMapper measurementFilterToPredicateMapper) {
    this.repository = repository;
    this.modelMapper = modelMapper;
    this.predicateMapper = measurementFilterToPredicateMapper;
  }

  @RequestMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return modelMapper.map(repository.findOne(id), MeasurementDto.class);
  }

  @RequestMapping()
  public Page<MeasurementDto> measurements(
    @RequestParam(value = "scale", required = false) String scale,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    Predicate filter = predicateMapper.map(requestParams);
    Page<MeasurementEntity> page;
    if (scale != null) {
      page = repository.findAllScaled(scale, filter, pageable);
    } else {
      page = repository.findAll(filter, pageable);
    }
    return page.map(source -> modelMapper.map(source, MeasurementDto.class));
  }
}
