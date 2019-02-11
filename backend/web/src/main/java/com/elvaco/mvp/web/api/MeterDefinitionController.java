package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/meter-definitions")
class MeterDefinitionController {
  private final MeterDefinitionUseCases meterDefinitionUseCases;

  @GetMapping
  public List<MeterDefinitionDto> meterDefinitions() {
    return meterDefinitionUseCases.findAll()
      .stream()
      .map(MeterDefinitionDtoMapper::toDto)
      .collect(toList());
  }
}
