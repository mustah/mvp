package com.elvaco.mvp.web.mapper;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.dto.QuantityDto;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toSet;

@UtilityClass
public class MeterDefinitionDtoMapper {
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

  private static QuantityDto toQuantityDto(DisplayQuantity displayQuantity) {
    return new QuantityDto(
      displayQuantity.quantity.name,
      displayQuantity.displayMode.equals(DisplayMode.CONSUMPTION),
      displayQuantity.unit,
      displayQuantity.decimals
    );
  }
}
