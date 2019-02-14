package com.elvaco.mvp.web.api;

import java.util.List;
import javax.validation.Valid;

import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.web.dto.MediumDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.dto.QuantityDto;
import com.elvaco.mvp.web.mapper.MediumDtoMapper;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;
import com.elvaco.mvp.web.mapper.QuantityDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper.toDto;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@RestApi("/api/v1/meter-definitions")
class MeterDefinitionController {
  private final MeterDefinitionUseCases meterDefinitionUseCases;
  private final MeterDefinitionDtoMapper meterDefinitionDtoMapper;

  @GetMapping
  public List<MeterDefinitionDto> meterDefinitions() {
    return meterDefinitionUseCases.findAll()
      .stream()
      .map(MeterDefinitionDtoMapper::toDto)
      .collect(toList());
  }

  @PostMapping
  public ResponseEntity<MeterDefinitionDto> createMeterDefinition(
    @Valid @RequestBody MeterDefinitionDto meterDefinitionDto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(toDto(
        meterDefinitionUseCases.save(meterDefinitionDtoMapper.toDomainModel(meterDefinitionDto))
      ));
  }

  @PutMapping
  public MeterDefinitionDto updateMeterDefinition(
    @RequestBody MeterDefinitionDto meterDefinitionDto
  ) {
    return toDto(meterDefinitionUseCases.update(
      meterDefinitionDtoMapper.toDomainModel(meterDefinitionDto)));
  }

  @DeleteMapping("{id}")
  public MeterDefinitionDto deleteMeterDefinition(@PathVariable Long id) {
    return toDto(meterDefinitionUseCases.deleteById(id));
  }

  @GetMapping("/quantities")
  public List<QuantityDto> quantities() {
    return meterDefinitionUseCases.findAllQuantities().stream()
      .map(QuantityDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("/medium")
  public List<MediumDto> medium() {
    return meterDefinitionUseCases.findAllMedium().stream()
      .map(MediumDtoMapper::toDto)
      .collect(toList());
  }
}
