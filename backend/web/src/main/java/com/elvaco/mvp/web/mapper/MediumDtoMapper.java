package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.web.dto.MediumDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MediumDtoMapper {

  public static MediumDto toDto(Medium medium) {
    return new MediumDto(medium.id, medium.name);
  }
}
