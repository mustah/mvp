package com.elvaco.mvp.web.mapper;

import java.util.Optional;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.web.dto.DisplayQuantityDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeterDefinitionDtoMapper {
  private final QuantityProvider quantityProvider;

  public static MeterDefinitionDto toDto(MeterDefinition meterDefinition) {
    return new MeterDefinitionDto(
      meterDefinition.id,
      meterDefinition.name,
      meterDefinition.quantities.stream()
        .map(MeterDefinitionDtoMapper::toQuantityDto)
        .collect(toSet()),
      Optional.ofNullable(meterDefinition.organisation)
        .map(OrganisationDtoMapper::toDto)
        .orElse(null),
      new IdNamedDto(
        meterDefinition.medium.id != null
          ? meterDefinition.medium.id.toString()
          : null, meterDefinition.medium.name),
      meterDefinition.autoApply

    );
  }

  public MeterDefinition toDomainModel(MeterDefinitionDto dto) {
    return new MeterDefinition(
      dto.id,
      Optional.ofNullable(dto.organisation).map(OrganisationDtoMapper::toDomainModel).orElse(null),
      dto.name,
      new Medium(Long.parseLong(dto.medium.id), dto.medium.name),
      dto.autoApply,
      dto.quantities.stream().map(this::toQuantityDomainModel).collect(toSet())
    );
  }

  private DisplayQuantity toQuantityDomainModel(DisplayQuantityDto displayQuantityDto) {
    return new DisplayQuantity(
      quantityProvider.getByNameOrThrow(displayQuantityDto.quanitityName),
      displayQuantityDto.consumption ? DisplayMode.CONSUMPTION : DisplayMode.READOUT,
      displayQuantityDto.precision,
      displayQuantityDto.displayUnit
    );
  }

  private static DisplayQuantityDto toQuantityDto(DisplayQuantity displayQuantity) {
    return new DisplayQuantityDto(
      displayQuantity.quantity.name,
      displayQuantity.displayMode.equals(DisplayMode.CONSUMPTION),
      displayQuantity.unit,
      displayQuantity.decimals
    );
  }
}
