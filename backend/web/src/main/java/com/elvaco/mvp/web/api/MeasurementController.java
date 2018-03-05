package com.elvaco.mvp.web.api;

import java.util.HashMap;
import java.util.List;

import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import com.elvaco.mvp.web.mapper.MeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final MeasurementMapper measurementMapper;

  @Autowired
  MeasurementController(
    MeasurementUseCases measurementUseCases,
    MeasurementMapper measurementMapper
  ) {
    this.measurementUseCases = measurementUseCases;
    this.measurementMapper = measurementMapper;
  }

  @GetMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return measurementUseCases.findById(id)
      .map(measurementMapper::toDto)
      .orElseThrow(() -> new MeasurementNotFound(id));
  }

  @GetMapping
  public List<MeasurementDto> measurements(
    @RequestParam(value = "scale", required = false) String scale,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return measurementUseCases.findAll(
      scale,
      new HashMap<>(requestParams)
    ).stream()
      .map(measurementMapper::toDto)
      .collect(toList());
  }
}
