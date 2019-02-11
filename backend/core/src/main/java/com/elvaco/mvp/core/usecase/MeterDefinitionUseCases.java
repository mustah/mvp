package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.exception.InvalidDisplayQuantity;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.unitconverter.UnitConverter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterDefinitionUseCases {
  private final AuthenticatedUser currentUser;
  private final MeterDefinitions meterDefinitions;
  private final UnitConverter unitConverter;

  public MeterDefinition save(MeterDefinition meterDefinition) {
    if (!currentUser.isSuperAdmin()
      && (!meterDefinition.belongsTo(currentUser.getOrganisationId()) || !currentUser.isAdmin())) {
      throw new Unauthorized("User is not authorized to save this entity");
    }

    meterDefinition.quantities
      .forEach(this::validateDisplayQuantity);
    return meterDefinitions.save(meterDefinition);
  }

  public List<MeterDefinition> findAll() {
    return meterDefinitions.findAll(
      currentUser.subOrganisationParameters().getEffectiveOrganisationId()
    );
  }

  private void validateDisplayQuantity(DisplayQuantity displayQuantity) {
    if (!unitConverter.isSameDimension(
      displayQuantity.quantity.storageUnit,
      displayQuantity.unit
    )) {
      throw new InvalidDisplayQuantity(
        String.format(
          "Invalid display unit '%s' for quantity '%s'",
          displayQuantity.unit,
          displayQuantity.quantity.name
        )
      );
    }
  }
}
