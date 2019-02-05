package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;

import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
      .body(MeterDefinitionDtoMapper.toDto(
        meterDefinitionUseCases.save(meterDefinitionDtoMapper.toDomainModel(meterDefinitionDto))
      ));
  }

  @PutMapping
  public MeterDefinitionDto updateMeterDefinition(
    @RequestBody MeterDefinitionDto meterDefinitionDto
  ) {
    throw new NotYetImplementedException();
  }

  @DeleteMapping("{id}")
  public ResponseEntity<MeterDefinitionDto> deleteMeterDefinition(@PathVariable UUID id) {
    throw new NotYetImplementedException();
  }
}
