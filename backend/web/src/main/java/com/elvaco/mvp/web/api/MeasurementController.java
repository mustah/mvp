package com.elvaco.mvp.web.api;

import java.util.HashMap;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final ModelMapper modelMapper;

  @Autowired
  MeasurementController(
    MeasurementUseCases measurementUseCases,
    ModelMapper modelMapper
  ) {
    this.measurementUseCases = measurementUseCases;
    this.modelMapper = modelMapper;
  }

  @GetMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return measurementUseCases.findById(id)
      .map(this::toDto)
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
      .map(this::toDto)
      .collect(toList());
  }

  private MeasurementDto toDto(Measurement measurement) {
    return modelMapper.map(measurement, MeasurementDto.class);
  }
}
