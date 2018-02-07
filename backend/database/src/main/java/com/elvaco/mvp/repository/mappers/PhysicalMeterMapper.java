package com.elvaco.mvp.repository.mappers;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;

public class PhysicalMeterMapper implements DomainEntityMapper<PhysicalMeter, PhysicalMeterEntity> {

  private final OrganisationMapper organisationMapper;

  public PhysicalMeterMapper(OrganisationMapper organisationMapper) {
    this.organisationMapper = organisationMapper;
  }

  @Override
  public PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return new PhysicalMeter(
      entity.id,
      organisationMapper.toDomainModel(entity.organisation),
      entity.identity,
      entity.medium
    );
  }

  @Override
  public PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(domainModel.id,
                                   organisationMapper.toEntity(domainModel.organisation),
                                   domainModel.identity,
                                   domainModel.medium);
  }
}
