package com.elvaco.mvp.database.repository.mappers;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

public class PhysicalMeterMapper implements DomainEntityMapper<PhysicalMeter, PhysicalMeterEntity> {

  private final OrganisationMapper organisationMapper;

  public PhysicalMeterMapper(
    OrganisationMapper organisationMapper
  ) {
    this.organisationMapper = organisationMapper;
  }

  @Override
  public PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, Optional.empty());
  }

  public PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity, Optional<Long> measurementCount) {
    return new PhysicalMeter(
      entity.id,
      organisationMapper.toDomainModel(entity.organisation),
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readInterval,
      measurementCount.orElse(null));
  }

  @Override
  public PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      organisationMapper.toEntity(domainModel.organisation),
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.readInterval
    );
  }
}
