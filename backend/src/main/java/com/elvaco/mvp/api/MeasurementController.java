package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.MeasurementRepository;

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
  MeasurementController(
    MeasurementRepository repository,
    ModelMapper modelMapper,
    MeasurementFilterToPredicateMapper predicateMapper
  ) {
    this.repository = repository;
    this.modelMapper = modelMapper;
    this.predicateMapper = predicateMapper;
  }

  @RequestMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return toDto(repository.findOne(id));
  }

  @RequestMapping()
  public Page<MeasurementDto> measurements(
    @RequestParam(value = "scale", required = false) String scale,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    Predicate predicate = predicateMapper.map(requestParams);
    Page<MeasurementEntity> page;
    if (scale != null) {
      page = repository.findAllScaled(scale, predicate, pageable);
    } else {
      page = repository.findAll(predicate, pageable);
    }
    return page.map(this::toDto);
  }

  private MeasurementDto toDto(MeasurementEntity entity) {
    return modelMapper.map(entity, MeasurementDto.class);
  }
}
