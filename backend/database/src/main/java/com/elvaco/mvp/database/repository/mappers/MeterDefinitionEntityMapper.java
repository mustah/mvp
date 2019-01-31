package com.elvaco.mvp.database.repository.mappers;

import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityEntity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityPk;
import com.elvaco.mvp.database.entity.meter.MediumEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeterDefinitionEntityMapper {

  private final QuantityEntityMapper quantityEntityMapper;

  public MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.id,
      Optional.ofNullable(domainModel.organisation)
        .map(OrganisationEntityMapper::toEntity)
        .orElse(null),
      toDisplayQuantityEntities(domainModel),
      domainModel.name,
      toMediumEntity(domainModel.medium),
      domainModel.autoApply
    );
  }

  public MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.id,
      Optional.ofNullable(entity.organisation)
        .map(OrganisationEntityMapper::toDomainModel)
        .orElse(null),
      entity.name,
      toMedium(entity.medium),
      entity.autoApply,
      entity.quantities.stream()
        .map(this::toDisplayQuantity)
        .collect(toSet())
    );
  }

  private DisplayQuantity toDisplayQuantity(
    DisplayQuantityEntity displayQuantityEntity
  ) {
    DisplayQuantityPk displayQuantityPk = displayQuantityEntity.getId();
    Quantity quantity = quantityEntityMapper.toDomainModel(displayQuantityPk.quantity);

    return new DisplayQuantity(
      quantity,
      displayQuantityPk.displayMode,
      displayQuantityEntity.displayUnit
    );
  }

  private Set<DisplayQuantityEntity> toDisplayQuantityEntities(
    MeterDefinition domainModel
  ) {
    return domainModel.quantities.stream()
      .map(displayQuantity -> toDisplayQuantityEntity(domainModel, displayQuantity))
      .collect(toSet());
  }

  private DisplayQuantityEntity toDisplayQuantityEntity(
    MeterDefinition meterDefinition,
    DisplayQuantity displayQuantity
  ) {
    DisplayQuantityPk pk = new DisplayQuantityPk(
      quantityEntityMapper.toEntity(displayQuantity.quantity),
      meterDefinition.id,
      displayQuantity.displayMode
    );
    return new DisplayQuantityEntity(
      pk,
      displayQuantity.unit,
      displayQuantity.decimals
    );
  }

  private Medium toMedium(MediumEntity mediumEntity) {
    return Medium.from(mediumEntity.name);
  }

  private MediumEntity toMediumEntity(Medium medium) {
    return new MediumEntity(medium.id, medium.name);
  }
}
