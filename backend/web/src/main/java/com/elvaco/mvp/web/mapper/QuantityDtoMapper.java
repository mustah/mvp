package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.QuantityDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QuantityDtoMapper {

  public static QuantityDto toDto(Quantity quanity) {
    return new QuantityDto(
      quanity.id,
      quanity.name
    );
  }
}
