package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class PhysicalMeterMapper implements DomainEntityMapper<PhysicalMeter, PhysicalMeterEntity> {

  private final OrganisationMapper organisationMapper;
  private final MeterStatusLogMapper meterStatusLogMapper;

  public PhysicalMeterMapper(
    OrganisationMapper organisationMapper,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.organisationMapper = organisationMapper;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  @Override
  public PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, Optional.empty());
  }

  public PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity, Optional<Long> measurementCount) {
    return toDomainModel(entity, measurementCount, emptyList());
  }

  public PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    Optional<Long> measurementCount,
    List<PhysicalMeterStatusLogEntity> statuses
  ) {
    return new PhysicalMeter(
      entity.id,
      organisationMapper.toDomainModel(entity.organisation),
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readInterval,
      measurementCount.orElse(null),
      statuses.stream().map(meterStatusLogMapper::toDomainModel).collect(toList())
    );
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
