package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.database.entity.meter.MediumEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediumEntityMapper {
  private final MediumProvider mediumProvider;

  public Medium toMedium(MediumEntity mediumEntity) {
    return mediumProvider.getByNameOrThrow(mediumEntity.name);
  }

  public MediumEntity toMediumEntity(Medium medium) {
    Medium existingMedium = mediumProvider.getByNameOrThrow(medium.name);
    return new MediumEntity(existingMedium.id, existingMedium.name);
  }
}
