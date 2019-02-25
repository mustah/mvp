package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.InvalidDisplayQuantity;
import com.elvaco.mvp.core.exception.InvalidMeterDefinition;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.unitconverter.UnitConverter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterDefinitionUseCases {

  private final AuthenticatedUser currentUser;
  private final MeterDefinitions meterDefinitions;
  private final UnitConverter unitConverter;
  private final Organisations organisations;
  private final QuantityProvider quantityProvider;
  private final MediumProvider mediumProvider;
  private final SystemMeterDefinitionProvider systemMeterDefinitionProvider;
  private final LogicalMeters logicalMeters;

  public MeterDefinition save(MeterDefinition meterDefinition) {
    if (meterDefinition.isDefault()) {
      throw new Unauthorized("System meter definitions can not be created");
    }
    return persist(meterDefinition, true);
  }

  public MeterDefinition update(MeterDefinition meterDefinition) {
    return persist(meterDefinition, false);
  }

  public List<MeterDefinition> findAll() {
    return meterDefinitions.findAll(
      currentUser.subOrganisationParameters().getEffectiveOrganisationId()
    );
  }

  public MeterDefinition deleteById(Long id) {
    return meterDefinitions.findById(id)
      .filter(md -> !md.isDefault())
      .filter(md -> hasAdminAccess(md))
      .map(
        md -> {
          resetToDefaultOnConnectedMeters(md);
          meterDefinitions.deleteById(id);
          return md;
        }
      ).orElseThrow(() -> new Unauthorized(
        "User is not authorized to delete meter definition with id " + id));
  }

  public List<Quantity> findAllQuantities() {
    return quantityProvider.all();
  }

  public List<Medium> findAllMedium() {
    return mediumProvider.all();
  }

  public MeterDefinition getAutoApplied(
    Organisation organisation,
    Medium medium
  ) {
    return getAutoApplied(organisation, medium, null);
  }

  private MeterDefinition getAutoApplied(
    Organisation organisation,
    Medium medium,
    MeterDefinition excluded
  ) {
    return meterDefinitions.findAll(organisation.id)
      .stream()
      .filter(md -> md.medium.name.equals(medium.name))
      .filter(md -> md.autoApply)
      .filter(md -> !md.isDefault())
      .filter(md -> excluded == null || !excluded.id.equals(md.id))
      .findFirst()
      .orElseGet(() -> systemMeterDefinitionProvider.getByMediumOrThrow(medium));
  }

  private MeterDefinition persist(MeterDefinition meterDefinition, boolean apply) {
    if (!hasAdminAccess(meterDefinition)) {
      throw new Unauthorized("User is not authorized to save this entity");
    }

    if (meterDefinition.organisation != null) {
      organisations.findById(meterDefinition.organisation.id)
        .filter(organisation -> organisation.parent != null)
        .ifPresent(organisation -> {
          throw new InvalidMeterDefinition("Meter definitions can not belong to sub-organisations");
        });
    }

    meterDefinition.quantities.forEach(this::validateDisplayQuantity);

    if (nameAlreadyExistingForOrganisation(meterDefinition)) {
      throw new InvalidMeterDefinition("Name must be unique for organisation");
    }

    if (meterDefinition.isDefault()
      && systemMeterDefinitionProvider.getByMedium(meterDefinition.medium)
      .filter(md -> !md.id.equals(meterDefinition.id))
      .isPresent()) {
      throw new InvalidMeterDefinition("Only one system meter definition per medium is allowed");
    }

    if (meterDefinition.autoApply
      && !getAutoApplied(meterDefinition.organisation, meterDefinition.medium).isDefault()) {
      throw new InvalidMeterDefinition(
        "Only one auto applied meter definition per organisation and medium is allowed");
    }

    MeterDefinition newMeterDefinition = meterDefinitions.save(meterDefinition);
    if (apply) {
      autoApplyOnExistingMeters(newMeterDefinition);
    }
    return newMeterDefinition;
  }

  private boolean nameAlreadyExistingForOrganisation(MeterDefinition meterDefinition) {
    if (meterDefinition.organisation == null) {
      return systemMeterDefinitionProvider.getByMedium(meterDefinition.medium)
        .filter(md -> !md.id.equals(meterDefinition.id))
        .filter(md -> md.name.equals(meterDefinition.name))
        .isPresent();
    } else {
      return meterDefinitions.findAll(meterDefinition.organisation.id)
        .stream()
        .filter(md -> !md.id.equals(meterDefinition.id))
        .anyMatch(md -> md.name.equals(meterDefinition.name));
    }
  }

  private boolean hasAdminAccess(MeterDefinition meterDefinition) {
    return currentUser.isSuperAdmin()
      || (currentUser.isAdmin() && meterDefinition.belongsTo(currentUser.getOrganisationId()));
  }

  private void autoApplyOnExistingMeters(MeterDefinition toMeterDefinition) {
    if (toMeterDefinition.autoApply && !toMeterDefinition.isDefault()) {
      logicalMeters.changeMeterDefinition(
        toMeterDefinition.organisation.id,
        systemMeterDefinitionProvider.getByMediumOrThrow(toMeterDefinition.medium),
        toMeterDefinition
      );
    }
  }

  private void resetToDefaultOnConnectedMeters(MeterDefinition fromMeterDefinition) {
    if (!fromMeterDefinition.isDefault()) {
      logicalMeters.changeMeterDefinition(
        fromMeterDefinition.organisation.id,
        fromMeterDefinition,
        getAutoApplied(
          fromMeterDefinition.organisation,
          fromMeterDefinition.medium,
          fromMeterDefinition
        )
      );
    }
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
